package com.mobigen.datafabric.dataLayer.service;

import com.mobigen.datafabric.dataLayer.model.OpenSearchModel;
import com.mobigen.datafabric.dataLayer.model.RecentSearchesModel;
import com.mobigen.datafabric.dataLayer.repository.OpenSearchRepository;
import com.mobigen.libs.configuration.Config;
import com.mobigen.libs.grpc.DataModel;
import com.mobigen.libs.grpc.Filter;
import lombok.Getter;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.MatchQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Getter
public class OpenSearchService {
    private List mustQuery;
    private List<Query> shouldQuery;
    private final OpenSearchRepository openSearchRepository;

    public OpenSearchService(OpenSearchRepository openSearchRepository) {
        this.openSearchRepository = openSearchRepository;
    }

    // TODO void to response?
    public List<String> search(String input, DataModel detailSearch, Filter filterSearch, String userId) {
        this.mustQuery = new ArrayList<>();
        this.shouldQuery = new ArrayList<>();
        mainQueryBuilder(input);
        // TODO detailSearch가 null일 때 이게 체크 되는지에 대한 Test필요
        if (detailSearch != null)
            mustQueryBuilder(detailSearch);

        if (filterSearch != null)
            shouldQueryBuilder(filterSearch);

        insertDocuemnt(input, userId); // for recentSearch

        return openSearchRepository.search(mustQuery, shouldQuery);
    }

    public OpenSearchService mainQueryBuilder(String input) {
        for (var field : OpenSearchModel.class.getDeclaredFields()) {
            if (field.getType() == String.class || field.getType() == String[].class)
                this.shouldQuery.add(new MatchQuery.Builder().field(field.getName()).query(FieldValue.of(input)).build()._toQuery());
        }

        for (var field : OpenSearchModel.Meta.class.getDeclaredFields())
            this.shouldQuery.add(new MatchQuery.Builder().field(field.getName()).query(FieldValue.of(input)).build()._toQuery());

        // TODO add Time compare(?);
        return this;
    }

    public OpenSearchService mustQueryBuilder(DataModel detailSearch) {
        for (var field : detailSearch.getDescriptorForType().getFields()) {
            if (!detailSearch.getField(field).toString().isEmpty()) {
                var name = field.getName();
                var fieldValue = detailSearch.getField(field);
                switch (fieldValue.getClass().getSimpleName()) { // TODO meta 관련 검색이 되는지에 대한 확인 필요
                    case "EmptyList":
                        break;
                    case "Time": // TODO
//                        var time = new SimpleDateFormat(((Time) fieldValue).getFormat()).format(((Time) fieldValue).getTime());
//                        var timeFilter = ((Time) fieldValue).getTimeOperator();
//                        switch (timeFilter) {
//                            case ">" -> sb.append(" AND ").append(name).append(":{").append(time).append(" TO *}");
//                            case "<" -> sb.append(" AND ").append(name).append(":{* TO").append(time).append("}");
//                            case ">=" -> sb.append(" AND ").append(name).append(":[").append(time).append(" TO *]");
//                            case "<=" -> sb.append(" AND ").append(name).append(":[* TO").append(time).append("]");
//                            default -> // == or =
//                                    sb.append(" AND ").append(name).append(time);
//                        }
                        break;
                    case "UnmodifiableRandomAccessList":
                        for (var value : (List) fieldValue)
                            this.mustQuery.add(new MatchQuery.Builder().field(name)
                                    .query(FieldValue.of(value.toString())).build()._toQuery());
                        break;
                    default:
                        this.mustQuery.add(new MatchQuery.Builder().field(name)
                                .query(FieldValue.of(fieldValue.toString())).build()._toQuery());
                        break;
                }
            }
        }
        return this;
    }

    public OpenSearchService shouldQueryBuilder(Filter filterSearch) {
        for (var field : filterSearch.getDescriptorForType().getFields()) {
            if (!filterSearch.getField(field).toString().isEmpty()) {
                var name = field.getName();
                var fieldValue = filterSearch.getField(field);

                for (var value : (List<String>) fieldValue) { // TODO meta 관련 Test진행
                    this.shouldQuery.add(new MatchQuery.Builder().field(name)
                            .query(FieldValue.of(value)).build()._toQuery());
                }
            }
        }
        return this;
    }

    public String[] getRecentSearch(String userId) {
        return openSearchRepository.getResentSearches(userId);
    }

    public void createIndex() {
        openSearchRepository.createIndex();
    }

    public void searchDocument(String[] dataModelIds) {
        // TODO getDocument to RDBMS
    }

    // todo datamodel
    public void insertDocument(OpenSearchModel openSearchModel) {
//        openSearchRepository.insertOpenDocument();
    }

    public void insertDocuemnt(String input, String userId) {
        var hit = openSearchRepository.search(userId); // todo error 뜨나?

        if (hit == null) {
            var arr = new LinkedList<String>();
            arr.add(input);
            var recentSearchModel = new RecentSearchesModel();
            recentSearchModel.setRecentSearches(arr.toArray(new String[0]));

            recentSearchModel.setUserId(userId);

            openSearchRepository.insertDocument(recentSearchModel);
        } else {
            var id = hit.id();
            var recentSearchModel = openSearchRepository.search(userId).source();
            var arr = new LinkedList<String>(List.of(recentSearchModel.getRecentSearches()));

            if (arr.size() < new Config().getConfig().getInt("open_search.recent_count")) {
                arr.addLast(input);
            } else {
                arr.pollFirst();
                arr.addFirst(input);
            }

            recentSearchModel.setRecentSearches(arr.toArray(new String[0]));
            openSearchRepository.updateDocument(recentSearchModel, id);
        }
    }

    // todo datamodel
    public void updateDocument() {

    }

    public void deleteDocument(String id) {
        openSearchRepository.deleteDocument(id);
    }

    public void SyncDocument() {
    }
}
