package com.whylabs.logging.core.utils;

import com.google.protobuf.ByteString;
import com.whylabs.logging.core.data.DatasetSummary;
import java.time.Instant;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import lombok.val;

@SuppressWarnings("unused")
@UtilityClass
public class ProtobufHelper {
  public String summaryToString(DatasetSummary summary) {
    val name = summary.getName();
    val tags =
        summary.getTagsList().asByteStringList().stream()
            .map(ByteString::toStringUtf8)
            .collect(Collectors.joining(","));
    val timestamp = Instant.ofEpochMilli(summary.getTimestamp()).toString();
    val columns = summary.getColumnsMap().keySet();

    return String.format(
        "Name: %s. Tags: %s. Timestamp: %s. Columns: %s", name, tags, timestamp, columns);
  }
}
