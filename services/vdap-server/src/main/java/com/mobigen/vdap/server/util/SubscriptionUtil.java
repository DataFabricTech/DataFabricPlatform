/*
 *  Copyright 2021 Collate
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.mobigen.vdap.server.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.openmetadata.common.utils.CommonUtil;
import org.openmetadata.schema.EntityInterface;
import org.openmetadata.schema.SubscriptionAction;
import org.openmetadata.schema.entity.events.StatusContext;
import org.openmetadata.schema.entity.events.SubscriptionDestination;
import org.openmetadata.schema.entity.events.TestDestinationStatus;
import org.openmetadata.schema.entity.feed.Thread;
import org.openmetadata.schema.entity.teams.Team;
import org.openmetadata.schema.entity.teams.User;
import org.openmetadata.schema.type.*;
import org.openmetadata.schema.type.profile.SubscriptionConfig;
import org.openmetadata.service.Entity;
import org.openmetadata.service.apps.bundles.changeEvent.Destination;
import org.openmetadata.service.events.subscription.AlertsRuleEvaluator;
import org.openmetadata.service.jdbi3.CollectionDAO;
import org.openmetadata.service.jdbi3.ListFilter;
import org.openmetadata.service.jdbi3.UserRepository;
import org.openmetadata.service.resources.feeds.MessageParser;
import org.openmetadata.service.security.SecurityUtil;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.openmetadata.common.utils.CommonUtil.nullOrEmpty;
import static org.openmetadata.service.Entity.*;
import static org.openmetadata.service.events.subscription.AlertsRuleEvaluator.getEntity;

@Slf4j
public class SubscriptionUtil {
  private SubscriptionUtil() {
    /* Hidden constructor */
  }

  /*
      This Method Return a list of Admin Emails or Slack/MsTeams/Generic/GChat Webhook Urls for Admin User
      DataInsightReport and EmailPublisher need a list of Emails, while others need a webhook Endpoint.
  */
  public static Set<String> getAdminsData(SubscriptionDestination.SubscriptionType type) {
    Set<String> data = new HashSet<>();
    UserRepository userEntityRepository = (UserRepository) Entity.getEntityRepository(USER);
    ResultList<User> result;
    ListFilter listFilter = new ListFilter(Include.ALL);
    listFilter.addQueryParam("isAdmin", "true");
    String after = null;
    try {
      do {
        result =
            userEntityRepository.listAfter(
                null, userEntityRepository.getFields("email,profile"), listFilter, 50, after);
        data.addAll(getEmailOrWebhookEndpointForUsers(result.getData(), type));
        after = result.getPaging().getAfter();
      } while (after != null);
    } catch (Exception ex) {
      LOG.error("Failed in listing all Users , Reason", ex);
    }
    return data;
  }

  public static Set<String> getEmailOrWebhookEndpointForUsers(
      List<User> users, SubscriptionDestination.SubscriptionType type) {
    if (type == SubscriptionDestination.SubscriptionType.EMAIL) {
      return users.stream().map(User::getEmail).collect(Collectors.toSet());
    } else {
      return users.stream()
          .map(user -> getWebhookUrlFromProfile(user.getProfile(), user.getId(), USER, type))
          .filter(Optional::isPresent)
          .map(Optional::get)
          .collect(Collectors.toSet());
    }
  }

  public static Set<String> getEmailOrWebhookEndpointForTeams(
      List<Team> users, SubscriptionDestination.SubscriptionType type) {
    if (type == SubscriptionDestination.SubscriptionType.EMAIL) {
      return users.stream().map(Team::getEmail).collect(Collectors.toSet());
    } else {
      return users.stream()
          .map(team -> getWebhookUrlFromProfile(team.getProfile(), team.getId(), TEAM, type))
          .filter(Optional::isPresent)
          .map(Optional::get)
          .collect(Collectors.toSet());
    }
  }

  /*
      This Method Return a list of Owner/Follower Emails or Slack/MsTeams/Generic/GChat Webhook Urls for Owner/Follower User
      of an Entity.
      DataInsightReport and EmailPublisher need a list of Emails, while others need a webhook Endpoint.
  */

  public static Set<String> getOwnerOrFollowers(
      SubscriptionDestination.SubscriptionType type,
      CollectionDAO daoCollection,
      UUID entityId,
      String entityType,
      Relationship relationship) {
    Set<String> data = new HashSet<>();
    try {
      List<CollectionDAO.EntityRelationshipRecord> ownerOrFollowers =
          daoCollection.relationshipDAO().findFrom(entityId, entityType, relationship.ordinal());
      // Users
      List<User> users =
          ownerOrFollowers.stream()
              .filter(e -> USER.equals(e.getType()))
              .map(user -> (User) Entity.getEntity(USER, user.getId(), "", Include.NON_DELETED))
              .toList();
      data.addAll(getEmailOrWebhookEndpointForUsers(users, type));

      // Teams
      List<Team> teams =
          ownerOrFollowers.stream()
              .filter(e -> TEAM.equals(e.getType()))
              .map(team -> (Team) Entity.getEntity(TEAM, team.getId(), "", Include.NON_DELETED))
              .toList();
      data.addAll(getEmailOrWebhookEndpointForTeams(teams, type));
    } catch (Exception ex) {
      LOG.error("Failed in listing all Owners/Followers, Reason : ", ex);
    }
    return data;
  }

  private static Set<String> getTaskAssignees(
      SubscriptionDestination.SubscriptionCategory category,
      SubscriptionDestination.SubscriptionType type,
      ChangeEvent event) {
    Thread thread = AlertsRuleEvaluator.getThread(event);
    Set<String> receiversList = new HashSet<>();
    Map<UUID, Team> teams = new HashMap<>();
    Map<UUID, User> users = new HashMap<>();

    Team tempTeamVar;
    User tempUserVar;

    if (category.equals(SubscriptionDestination.SubscriptionCategory.ASSIGNEES)) {
      List<EntityReference> assignees = thread.getTask().getAssignees();
      if (!nullOrEmpty(assignees)) {
        for (EntityReference reference : assignees) {
          if (Entity.USER.equals(reference.getType())) {
            tempUserVar = Entity.getEntity(USER, reference.getId(), "profile", Include.NON_DELETED);
            users.put(tempUserVar.getId(), tempUserVar);
          } else if (TEAM.equals(reference.getType())) {
            tempTeamVar = Entity.getEntity(TEAM, reference.getId(), "profile", Include.NON_DELETED);
            teams.put(tempTeamVar.getId(), tempTeamVar);
          }
        }
      }

      for (Post post : thread.getPosts()) {
        tempUserVar = Entity.getEntityByName(USER, post.getFrom(), "profile", Include.NON_DELETED);
        users.put(tempUserVar.getId(), tempUserVar);
        List<MessageParser.EntityLink> mentions = MessageParser.getEntityLinks(post.getMessage());
        for (MessageParser.EntityLink link : mentions) {
          if (USER.equals(link.getEntityType())) {
            tempUserVar = Entity.getEntity(link, "profile", Include.NON_DELETED);
            users.put(tempUserVar.getId(), tempUserVar);
          } else if (TEAM.equals(link.getEntityType())) {
            tempTeamVar = Entity.getEntity(link, "profile", Include.NON_DELETED);
            teams.put(tempTeamVar.getId(), tempTeamVar);
          }
        }
      }
    }

    if (category.equals(SubscriptionDestination.SubscriptionCategory.OWNERS)) {
      try {
        tempUserVar =
            Entity.getEntityByName(USER, thread.getCreatedBy(), "profile", Include.NON_DELETED);
        users.put(tempUserVar.getId(), tempUserVar);
      } catch (Exception ex) {
        LOG.warn("Thread created by unknown user: {}", thread.getCreatedBy());
      }
    }

    // Users
    receiversList.addAll(getEmailOrWebhookEndpointForUsers(users.values().stream().toList(), type));

    // Teams
    receiversList.addAll(getEmailOrWebhookEndpointForTeams(teams.values().stream().toList(), type));

    return receiversList;
  }

  public static Set<String> handleConversationNotification(
      SubscriptionDestination.SubscriptionCategory category,
      SubscriptionDestination.SubscriptionType type,
      ChangeEvent event) {
    Thread thread = AlertsRuleEvaluator.getThread(event);
    Set<String> receiversList = new HashSet<>();
    Map<UUID, Team> teams = new HashMap<>();
    Map<UUID, User> users = new HashMap<>();

    Team tempTeamVar;
    User tempUserVar;

    if (category.equals(SubscriptionDestination.SubscriptionCategory.MENTIONS)) {
      List<MessageParser.EntityLink> mentions = MessageParser.getEntityLinks(thread.getMessage());
      for (MessageParser.EntityLink link : mentions) {
        if (USER.equals(link.getEntityType())) {
          tempUserVar = Entity.getEntity(link, "profile", Include.NON_DELETED);
          users.put(tempUserVar.getId(), tempUserVar);
        } else if (TEAM.equals(link.getEntityType())) {
          tempTeamVar = Entity.getEntity(link, "", Include.NON_DELETED);
          teams.put(tempTeamVar.getId(), tempTeamVar);
        }
      }

      for (Post post : thread.getPosts()) {
        tempUserVar = Entity.getEntityByName(USER, post.getFrom(), "profile", Include.NON_DELETED);
        users.put(tempUserVar.getId(), tempUserVar);
        mentions = MessageParser.getEntityLinks(post.getMessage());
        for (MessageParser.EntityLink link : mentions) {
          if (USER.equals(link.getEntityType())) {
            tempUserVar = Entity.getEntity(link, "profile", Include.NON_DELETED);
            users.put(tempUserVar.getId(), tempUserVar);
          } else if (TEAM.equals(link.getEntityType())) {
            tempTeamVar = Entity.getEntity(link, "profile", Include.NON_DELETED);
            teams.put(tempTeamVar.getId(), tempTeamVar);
          }
        }
      }
    }

    if (category.equals(SubscriptionDestination.SubscriptionCategory.OWNERS)) {
      try {
        tempUserVar =
            Entity.getEntityByName(USER, thread.getCreatedBy(), "profile", Include.NON_DELETED);
        users.put(tempUserVar.getId(), tempUserVar);
      } catch (Exception ex) {
        LOG.warn("Thread created by unknown user: {}", thread.getCreatedBy());
      }
    }

    // Users
    receiversList.addAll(getEmailOrWebhookEndpointForUsers(users.values().stream().toList(), type));

    // Teams
    receiversList.addAll(getEmailOrWebhookEndpointForTeams(teams.values().stream().toList(), type));

    return receiversList;
  }

  private static Optional<String> getWebhookUrlFromProfile(
      Profile profile, UUID id, String entityType, SubscriptionDestination.SubscriptionType type) {
    if (profile != null) {
      SubscriptionConfig subscriptionConfig = profile.getSubscription();
      if (subscriptionConfig != null) {
        Webhook webhookConfig =
            switch (type) {
              case SLACK -> profile.getSubscription().getSlack();
              case MS_TEAMS -> profile.getSubscription().getMsTeams();
              case G_CHAT -> profile.getSubscription().getgChat();
              case WEBHOOK -> profile.getSubscription().getGeneric();
              default -> null;
            };
        if (webhookConfig != null && !CommonUtil.nullOrEmpty(webhookConfig.getEndpoint())) {
          return Optional.of(webhookConfig.getEndpoint().toString());
        } else {
          LOG.debug(
              "[GetWebhookUrlsFromProfile] Owner with id {} type {}, will not get any Notification as not webhook config is missing for type {}, webhookConfig {} ",
              id,
              entityType,
              type.value(),
              webhookConfig);
        }
      }
    }
    LOG.debug(
        "[GetWebhookUrlsFromProfile] Failed to Get Profile for Owner with ID : {} and type {} ",
        id,
        type);
    return Optional.empty();
  }

  public static Set<String> buildReceiversListFromActions(
      SubscriptionAction action,
      SubscriptionDestination.SubscriptionCategory category,
      SubscriptionDestination.SubscriptionType type,
      CollectionDAO daoCollection,
      UUID entityId,
      String entityType) {
    Set<String> receiverList = new HashSet<>();

    if (category.equals(SubscriptionDestination.SubscriptionCategory.USERS)) {
      if (nullOrEmpty(action.getReceivers())) {
        throw new IllegalArgumentException(
            "Email Alert Invoked with Illegal Type and Settings. Emtpy or Null Users Recipients List");
      }
      List<User> users =
          action.getReceivers().stream()
              .map(user -> (User) Entity.getEntityByName(USER, user, "", Include.NON_DELETED))
              .toList();
      receiverList.addAll(getEmailOrWebhookEndpointForUsers(users, type));
    } else if (category.equals(SubscriptionDestination.SubscriptionCategory.TEAMS)) {
      if (nullOrEmpty(action.getReceivers())) {
        throw new IllegalArgumentException(
            "Email Alert Invoked with Illegal Type and Settings. Emtpy or Null Teams Recipients List");
      }
      List<Team> teams =
          action.getReceivers().stream()
              .map(team -> (Team) Entity.getEntityByName(TEAM, team, "", Include.NON_DELETED))
              .toList();
      receiverList.addAll(getEmailOrWebhookEndpointForTeams(teams, type));
    } else {
      receiverList = action.getReceivers() == null ? receiverList : action.getReceivers();
    }

    // Send to Admins
    if (Boolean.TRUE.equals(action.getSendToAdmins())) {
      receiverList.addAll(getAdminsData(type));
    }

    // Send To Owners
    if (Boolean.TRUE.equals(action.getSendToOwners())) {
      receiverList.addAll(
          getOwnerOrFollowers(type, daoCollection, entityId, entityType, Relationship.OWNS));
    }

    // Send To Followers
    if (Boolean.TRUE.equals(action.getSendToFollowers())) {
      receiverList.addAll(
          getOwnerOrFollowers(type, daoCollection, entityId, entityType, Relationship.FOLLOWS));
    }

    return receiverList;
  }

  public static Set<String> getTargetsForAlert(
      SubscriptionAction action,
      SubscriptionDestination.SubscriptionCategory category,
      SubscriptionDestination.SubscriptionType type,
      ChangeEvent event) {
    Set<String> receiverUrls = new HashSet<>();
    if (event.getEntityType().equals(THREAD)) {
      Thread thread = AlertsRuleEvaluator.getThread(event);
      switch (thread.getType()) {
        case Task -> receiverUrls.addAll(getTaskAssignees(category, type, event));
        case Conversation -> receiverUrls.addAll(
            handleConversationNotification(category, type, event));
          // TODO: For Announcement, Immediate Consumer needs to be Notified (find information from
          // Lineage)
        case Announcement -> {
          receiverUrls.addAll(buildReceivers(action, category, type, event, event.getEntityId()));
        }
      }
    } else {
      EntityInterface entityInterface = getEntity(event);
      receiverUrls.addAll(buildReceivers(action, category, type, event, entityInterface.getId()));
    }

    return receiverUrls;
  }

  private static Set<String> buildReceivers(
      SubscriptionAction action,
      SubscriptionDestination.SubscriptionCategory category,
      SubscriptionDestination.SubscriptionType type,
      ChangeEvent event,
      UUID id) {
    Set<String> result = new HashSet<>();
    result.addAll(
        buildReceiversListFromActions(
            action, category, type, Entity.getCollectionDAO(), id, event.getEntityType()));
    return result;
  }

  public static List<Invocation.Builder> getTargetsForWebhookAlert(
      SubscriptionAction action,
      SubscriptionDestination.SubscriptionCategory category,
      SubscriptionDestination.SubscriptionType type,
      Client client,
      ChangeEvent event) {
    List<Invocation.Builder> targets = new ArrayList<>();
    for (String url : getTargetsForAlert(action, category, type, event)) {
      targets.add(appendHeadersToTarget(client, url));
    }
    return targets;
  }

  public static Invocation.Builder appendHeadersToTarget(Client client, String uri) {
    Map<String, String> authHeaders = SecurityUtil.authHeaders("admin@open-metadata.org");
    return SecurityUtil.addHeaders(client.target(uri), authHeaders);
  }

  public static void postWebhookMessage(
      Destination<ChangeEvent> destination, Invocation.Builder target, Object message) {
    postWebhookMessage(destination, target, message, Webhook.HttpMethod.POST);
  }

  public static void postWebhookMessage(
      Destination<ChangeEvent> destination,
      Invocation.Builder target,
      Object message,
      Webhook.HttpMethod httpMethod) {
    long attemptTime = System.currentTimeMillis();
    Response response =
        (httpMethod == Webhook.HttpMethod.PUT)
            ? target.put(javax.ws.rs.client.Entity.entity(message, MediaType.APPLICATION_JSON_TYPE))
            : target.post(
                javax.ws.rs.client.Entity.entity(message, MediaType.APPLICATION_JSON_TYPE));

    LOG.debug(
        "Subscription Destination HTTP Operation {}:{} received response {}",
        httpMethod,
        destination.getSubscriptionDestination().getId(),
        response.getStatusInfo());

    StatusContext statusContext = createStatusContext(response);
    handleStatus(destination, attemptTime, statusContext);
  }

  public static void deliverTestWebhookMessage(
      Destination<ChangeEvent> destination, Invocation.Builder target, Object message) {
    deliverTestWebhookMessage(destination, target, message, Webhook.HttpMethod.POST);
  }

  public static void deliverTestWebhookMessage(
      Destination<ChangeEvent> destination,
      Invocation.Builder target,
      Object message,
      Webhook.HttpMethod httpMethod) {
    Response response =
        (httpMethod == Webhook.HttpMethod.PUT)
            ? target.put(javax.ws.rs.client.Entity.entity(message, MediaType.APPLICATION_JSON_TYPE))
            : target.post(
                javax.ws.rs.client.Entity.entity(message, MediaType.APPLICATION_JSON_TYPE));

    StatusContext statusContext = createStatusContext(response);
    handleTestDestinationStatus(destination, statusContext);
  }

  private static void handleTestDestinationStatus(
      Destination<ChangeEvent> destination, StatusContext statusContext) {
    TestDestinationStatus.Status testStatus =
        (statusContext.getStatusCode() == 200)
            ? TestDestinationStatus.Status.SUCCESS
            : TestDestinationStatus.Status.FAILED;

    destination.setStatusForTestDestination(testStatus, statusContext);
  }

  private static void handleStatus(
      Destination<ChangeEvent> destination, long attemptTime, StatusContext statusContext) {
    int statusCode = statusContext.getStatusCode();
    String statusInfo = statusContext.getStatusInfo();

    if (statusCode >= 300 && statusCode < 400) {
      // 3xx response/redirection is not allowed for callback. Set the webhook state as in error
      destination.setErrorStatus(attemptTime, statusCode, statusInfo);
    } else if (statusCode == 200) {
      destination.setSuccessStatus(System.currentTimeMillis());
    } else {
      // 4xx, 5xx response retry delivering events after timeout
      destination.setAwaitingRetry(attemptTime, statusCode, statusInfo);
    }
  }

  private static StatusContext createStatusContext(Response response) {
    return new StatusContext()
        .withStatusCode(response.getStatus())
        .withStatusInfo(response.getStatusInfo().getReasonPhrase())
        .withHeaders(response.getStringHeaders())
        .withEntity(response.hasEntity() ? response.readEntity(String.class) : StringUtils.EMPTY)
        .withMediaType(
            response.getMediaType() != null
                ? response.getMediaType().toString()
                : StringUtils.EMPTY)
        .withLocation(
            response.getLocation() != null ? response.getLocation().toString() : StringUtils.EMPTY)
        .withTimestamp(System.currentTimeMillis());
  }

  public static Client getClient(int connectTimeout, int readTimeout) {
    ClientBuilder clientBuilder = ClientBuilder.newBuilder();
    clientBuilder.connectTimeout(connectTimeout, TimeUnit.SECONDS);
    clientBuilder.readTimeout(readTimeout, TimeUnit.SECONDS);
    return clientBuilder.build();
  }
}
