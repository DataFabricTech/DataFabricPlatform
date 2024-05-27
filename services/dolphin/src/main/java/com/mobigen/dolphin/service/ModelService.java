package com.mobigen.dolphin.service;

import com.mobigen.dolphin.entity.openmetadata.DBServiceEntity;
import com.mobigen.dolphin.entity.request.CreateModelDto;
import com.mobigen.dolphin.entity.response.ModelDto;
import com.mobigen.dolphin.repository.openmetadata.OMRepository;
import com.mobigen.dolphin.repository.trino.TrinoRepository;
import com.mobigen.dolphin.util.ModelType;
import lombok.RequiredArgsConstructor;
import org.openmetadata.service.fernet.Fernet;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@RequiredArgsConstructor
@Service
public class ModelService {
    private final OMRepository omRepository;
    private final TrinoRepository trinoRepository;

    public List<ModelDto> getModels() {
        return trinoRepository.getModelList();
    }

    public DBServiceEntity getConnectorInfo(UUID id) {
        var connInfo = omRepository.findById(id);
        if (connInfo.isEmpty()) {
            throw new RuntimeException("No connector found for id " + id);
        }
        return connInfo.get();
    }

    private String decryptOpenMetadata(String data) {
        var fernet = Fernet.getInstance();
        fernet.setFernetKey("jJ/9sz0g0OHxsfxOoSfdFdmk3ysNmPRnH3TUAbz3IHA=");
        return fernet.decrypt("fernet:gAAAAABmUBVnDWQH1O7e7NtPHZfPaXomGDYhMc6yEFGvUKkoHST33lo_LSA2S635FWyCA2XI1OytlamIc04qcTYc2pnzJCZ4Yg==");
    }

    private String getOrCreateTrinoCatalog(DBServiceEntity dbServiceEntity) {
        var connInfo = dbServiceEntity.getJson().getConnection().get("config");
        var jdbcURL = "jdbc:" + connInfo.get("schema") + "://"
                + connInfo.get("host") + ":" + connInfo.get("hostPort");
        var username = connInfo.get("username");
        var password = decryptOpenMetadata((String) connInfo.get("password"));
        var catalogName = dbServiceEntity.getId().toString().replace("-", "_");
        var createQuery = "create catalog " + catalogName +
                " using " + dbServiceEntity.getServiceType() +
                " with (" +
                " \"connection-url\" = " + jdbcURL + ", " +
                " \"connection-user\" = " + username + ", " +
                " \"connection-password\" = " + password + ")";
        trinoRepository.execute(createQuery);
        return catalogName;
    }

    public ModelDto createModel(CreateModelDto createModelDto) {
        var selectedColumns = !createModelDto.getBaseModel().getSelectedColumnNames().isEmpty() ?
                String.join(", ", createModelDto.getBaseModel().getSelectedColumnNames()) : "*";
        String sql = "create view " + createModelDto.getModelName();
        if (createModelDto.getBaseModel().getType() == ModelType.CONNECTOR) {
            var connInfo = getConnectorInfo(createModelDto.getBaseModel().getConnectorId());
            System.out.println(connInfo);
            sql = sql + " as select " + selectedColumns
                    + " from " + createModelDto.getBaseModel().getTable();
        } else if (createModelDto.getBaseModel().getType() == ModelType.MODEL) {
            sql = sql + " as select " + selectedColumns
                    + " from " + createModelDto.getBaseModel().getModel();
        } else {
            sql = sql + " as " + createModelDto.getBaseModel().getQuery();
        }
        System.out.println(sql);
        return null;
    }

    public static void main(String[] args) {
        var fernet = Fernet.getInstance();
        fernet.setFernetKey("jJ/9sz0g0OHxsfxOoSfdFdmk3ysNmPRnH3TUAbz3IHA=");
        var decrypted = fernet.decrypt("fernet:gAAAAABmUBVnDWQH1O7e7NtPHZfPaXomGDYhMc6yEFGvUKkoHST33lo_LSA2S635FWyCA2XI1OytlamIc04qcTYc2pnzJCZ4Yg==");
        System.out.println(decrypted);
    }
}
