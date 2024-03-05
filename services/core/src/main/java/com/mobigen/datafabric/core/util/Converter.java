package com.mobigen.datafabric.core.util;

import com.mobigen.datafabric.share.interfaces.*;
import dto.enums.StatusType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;

public class Converter {
    // DateTime
    public LocalDateTime convert(DateTime dateTime) {
        return switch (dateTime.getDateTimeCase()) {
            case STR_DATE_TIME -> LocalDateTime.parse(dateTime.getStrDateTime());
            case UTC_TIME ->
                    Instant.ofEpochMilli(dateTime.getUtcTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
            case DATETIME_NOT_SET -> null;
        };
    }

    public DateTime convert(LocalDateTime localDateTime) {
        return localDateTime != null ?
                DateTime.newBuilder()
                        .setUtcTime(localDateTime.atZone(ZoneOffset.UTC).toInstant().toEpochMilli()).build():
                DateTime.newBuilder().build();
    }

    // Status
    public StatusType convert(Status connectStatus) {
        return switch (connectStatus) {
            case CONNECTED -> StatusType.CONNECTED;
            case DISCONNECTED -> StatusType.DISCONNECTED;
            case ERROR -> StatusType.ERROR;
            case SYNC -> StatusType.SYNC;
            case INIT -> StatusType.INIT;
            case UNRECOGNIZED -> null;
        };
    }

    public Status convert(StatusType statusType) {
        return switch (statusType) {
            case CONNECTED -> Status.CONNECTED;
            case DISCONNECTED -> Status.DISCONNECTED;
            case ERROR -> Status.ERROR;
            case SYNC -> Status.SYNC;
            case INIT -> Status.INIT;
        };
    }

    // FormatType
    public dto.enums.FormatType convert(FormatType formatType) {
        return switch (formatType) {
            case CSV -> dto.enums.FormatType.CSV;
            case EXCEL -> dto.enums.FormatType.EXCEL;
            case WORD -> dto.enums.FormatType.WORD;
            case TABLE -> dto.enums.FormatType.TABLE;
            case VIEW -> dto.enums.FormatType.VIEW;
            case INDEX -> dto.enums.FormatType.INDEX;
            case TXT -> dto.enums.FormatType.TXT;
            case HWP -> dto.enums.FormatType.HWP;
            case DB -> dto.enums.FormatType.DB;
            case DIRECTORY -> dto.enums.FormatType.DIRECTORY;
            case FILE -> dto.enums.FormatType.FILE;
            case UNRECOGNIZED -> null;
        };
    }

    public FormatType convert(dto.enums.FormatType formatType) {
        return switch (formatType) {
            case CSV -> FormatType.CSV;
            case EXCEL -> FormatType.EXCEL;
            case WORD -> FormatType.WORD;
            case TABLE -> FormatType.TABLE;
            case VIEW -> FormatType.VIEW;
            case INDEX -> FormatType.INDEX;
            case TXT -> FormatType.TXT;
            case HWP -> FormatType.HWP;
            case DB -> FormatType.DB;
            case DIRECTORY -> FormatType.DIRECTORY;
            case FILE -> FormatType.FILE;
        };
    }

    // pageable
    public PageRequest convertPageable(Pageable pageable) {
        Sort sort = null;
        var sortList = new ArrayList<>(pageable.getSortList());
        sortList.sort(Comparator.comparingInt(com.mobigen.datafabric.share.interfaces.Sort::getOrder));
        for (var grpcSort : sortList) {
            Sort currentSort = switch (grpcSort.getDirection()) {
                case ASC -> Sort.by(grpcSort.getField()).ascending();
                case DESC -> Sort.by(grpcSort.getField()).descending();
                case UNRECOGNIZED -> null;
            };

            if (currentSort != null) {
                sort = sort == null ? currentSort : sort.and(currentSort);
            }
        }
        return pageable.getPage().getSelectPage() == 0 || pageable.getPage().getSize() == 0 ?
                null : sort != null ?
                PageRequest.of(pageable.getPage().getSelectPage(), pageable.getPage().getSize(), sort) :
                PageRequest.of(pageable.getPage().getSelectPage(), pageable.getPage().getSize());

    }

    // DataType
    public DataType convert(dto.enums.DataType dataType) {
        return switch (dataType) {
            case NUMBER -> DataType.NUMBER;
            case TEXT -> DataType.TEXT;
            case VARCHAR -> DataType.VARCHAR;
            case INT4 -> DataType.INT32;
            case STRING -> DataType.STRING;
            case BOOLEAN -> DataType.BOOL;
            case INT8 -> DataType.INT64;
            case BYTES -> DataType.BYTES;
            case FLOAT -> DataType.FLOAT;
            case DOUBLE -> DataType.DOUBLE;
            case DATETIME -> DataType.DATETIME;
        };
    }

    public dto.enums.DataType convert(DataType dataType) {
        return switch (dataType) {
            case STRING -> dto.enums.DataType.STRING;
            case INT32 -> dto.enums.DataType.INT4;
            case INT64 -> dto.enums.DataType.INT8;
            case BOOL -> dto.enums.DataType.BOOLEAN;
            case BYTES -> dto.enums.DataType.BYTES;
            case FLOAT -> dto.enums.DataType.FLOAT;
            case DOUBLE -> dto.enums.DataType.DOUBLE;
            case DATETIME -> dto.enums.DataType.DATETIME;
            case VARCHAR -> dto.enums.DataType.VARCHAR;
            case TEXT -> dto.enums.DataType.TEXT;
            case NUMBER -> dto.enums.DataType.NUMBER;
            case UNRECOGNIZED -> null;
        };
    }

    // QualityType
    public dto.enums.QualityType convert(QualityType qualityType) {
        return switch (qualityType) {
            case MIN -> dto.enums.QualityType.MIN;
            case MAX -> dto.enums.QualityType.MAX;
            case SUM -> dto.enums.QualityType.SUM;
            case UNRECOGNIZED -> null;
        };
    }

    public QualityType convert(dto.enums.QualityType qualityType) {
        return switch (qualityType) {
            case MIN -> QualityType.MIN;
            case MAX -> QualityType.MAX;
            case SUM -> QualityType.SUM;
        };
    }
}