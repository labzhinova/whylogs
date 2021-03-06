package com.whylabs.logging.core;

import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import com.google.protobuf.ByteString;
import com.whylabs.logging.core.iterator.ColumnsChunkSegmentIterator;
import com.whylabs.logging.core.message.*;
import com.whylabs.logging.core.message.ColumnSummary;
import com.whylabs.logging.core.message.DatasetProperties.Builder;
import com.whylabs.logging.core.message.DatasetSummary;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import lombok.val;

public class DatasetProfile implements Serializable {
  // generated by IntelliJ
  private static final long serialVersionUID = -9221998596693275458L;

  @Getter String sessionId;
  @Getter Instant sessionTimestamp;
  @Getter Instant dataTimestamp;
  // always sorted
  @Getter List<String> tags;
  Map<String, ColumnProfile> columns;
  Map<String, String> metadata;

  /**
   * DEVELOPER API. DO NOT USE DIRECTLY
   *
   * @param sessionId dataset name
   * @param sessionTimestamp the timestamp for the current profiling session
   * @param dataTimestamp the timestamp for the dataset. Used to aggregate across different cadences
   * @param tags tags of the dataset
   * @param columns the columns that we're copying over. Note that the source of columns should stop
   *     using these column objects as they will back this DatasetProfile instead
   */
  public DatasetProfile(
      @NonNull String sessionId,
      @NonNull Instant sessionTimestamp,
      @Nullable Instant dataTimestamp,
      @NonNull List<String> tags,
      @NonNull Map<String, ColumnProfile> columns) {
    this.sessionId = sessionId;
    this.sessionTimestamp = sessionTimestamp;
    this.dataTimestamp = dataTimestamp;
    this.columns = new ConcurrentHashMap<>();
    this.tags = ImmutableList.sortedCopyOf(Sets.newHashSet(tags));
    this.metadata = new ConcurrentHashMap<>();
    this.columns = new ConcurrentHashMap<>(columns);
  }

  /**
   * Create a new Dataset profile
   *
   * @param sessionId the name of the dataset profile
   * @param sessionTimestamp the timestamp for this run
   * @param tags the tags to track the dataset with
   */
  public DatasetProfile(
      @NonNull String sessionId, @NonNull Instant sessionTimestamp, @NonNull List<String> tags) {
    this(sessionId, sessionTimestamp, null, tags, Collections.emptyMap());
  }

  public DatasetProfile(String sessionId, Instant sessionTimestamp) {
    this(sessionId, sessionTimestamp, Collections.emptyList());
  }

  public Map<String, ColumnProfile> getColumns() {
    return Collections.unmodifiableMap(columns);
  }

  public DatasetProfile withMetadata(String key, String value) {
    this.metadata.put(key, value);
    return this;
  }

  public DatasetProfile withAllMetadata(Map<String, String> metadata) {
    this.metadata.putAll(metadata);
    return this;
  }

  private void validate() {
    Preconditions.checkNotNull(sessionId);
    Preconditions.checkNotNull(sessionTimestamp);
    Preconditions.checkNotNull(columns);
    Preconditions.checkNotNull(metadata);
    Preconditions.checkNotNull(tags);
    Preconditions.checkState(
        Ordering.natural().isOrdered(this.tags), "Tags should be sorted %s", this.tags);
  }

  public void track(String columnName, Object data) {
    trackSingleColumn(columnName, data);
  }

  private void trackSingleColumn(String columnName, Object data) {
    val columnProfile = columns.computeIfAbsent(columnName, ColumnProfile::new);
    columnProfile.track(data);
  }

  public void track(Map<String, ?> columns) {
    columns.forEach(this::track);
  }

  public DatasetSummary toSummary() {
    validate();

    val summaryColumns =
        columns.values().stream()
            .map(Pair::fromColumn)
            .collect(Collectors.toMap(Pair::getName, Pair::getStatistics));

    val summary =
        DatasetSummary.newBuilder()
            .setProperties(toDatasetProperties())
            .putAllColumns(summaryColumns);

    return summary.build();
  }

  public Iterator<MessageSegment> toChunkIterator() {
    validate();

    final String marker = sessionId + UUID.randomUUID().toString();

    // first segment is the metadata
    val properties = toDatasetProperties();
    val metadataBuilder =
        DatasetMetadataSegment.newBuilder().setProperties(properties).setMarker(marker);
    val metadataSegment =
        MessageSegment.newBuilder().setMarker(marker).setMetadata(metadataBuilder).build();

    // then we group the columns by size
    val chunkedColumns =
        columns.values().stream()
            .map(ColumnProfile::toProtobuf)
            .map(ColumnMessage.Builder::build)
            .iterator();

    val columnSegmentMessages =
        Iterators.<ColumnsChunkSegment, MessageSegment>transform(
            new ColumnsChunkSegmentIterator(chunkedColumns, marker),
            msg -> MessageSegment.newBuilder().setColumns(msg).build());

    return Iterators.concat(Iterators.singletonIterator(metadataSegment), columnSegmentMessages);
  }

  public DatasetProfile merge(@NonNull DatasetProfile other) {
    this.validate();
    other.validate();

    Preconditions.checkArgument(
        Objects.equals(this.sessionId, other.sessionId),
        "Mismatched name. Current name [%s] is merged with [%s]",
        this.sessionId,
        other.sessionId);
    Preconditions.checkArgument(
        Objects.equals(this.sessionTimestamp, other.sessionTimestamp),
        "Mismatched session timestamp. Current ts [%s] is merged with [%s]",
        this.sessionTimestamp,
        other.sessionTimestamp);

    Preconditions.checkArgument(
        Objects.equals(this.dataTimestamp, other.dataTimestamp),
        "Mismatched data timestamp. Current ts [%s] is merged with [%s]",
        this.dataTimestamp,
        other.dataTimestamp);
    Preconditions.checkArgument(
        Objects.equals(this.tags, other.tags),
        "Mismatched tags. Current %s being merged with %s",
        this.tags,
        other.tags);

    val result =
        new DatasetProfile(
            this.sessionId,
            this.sessionTimestamp,
            this.dataTimestamp,
            this.tags,
            Collections.emptyMap());

    val unionColumns = Sets.union(this.columns.keySet(), other.columns.keySet());
    for (String column : unionColumns) {
      val emptyColumn = new ColumnProfile(column);
      val thisColumn = this.columns.getOrDefault(column, emptyColumn);
      val otherColumn = other.columns.getOrDefault(column, emptyColumn);

      result.columns.put(column, thisColumn.merge(otherColumn));
    }

    return result;
  }

  public DatasetProfileMessage.Builder toProtobuf() {
    validate();
    val properties = toDatasetProperties();

    val builder = DatasetProfileMessage.newBuilder().setProperties(properties);

    columns.forEach((k, v) -> builder.putColumns(k, v.toProtobuf().build()));
    return builder;
  }

  private Builder toDatasetProperties() {
    val dataTimeInMillis = (dataTimestamp == null) ? -1L : dataTimestamp.toEpochMilli();
    return DatasetProperties.newBuilder()
        .setSessionId(sessionId)
        .setSessionTimestamp(sessionTimestamp.toEpochMilli())
        .setDataTimestamp(dataTimeInMillis)
        .addAllTags(tags)
        .putAllMetadata(metadata)
        .setSchemaMajorVersion(SchemaInformation.SCHEMA_MAJOR_VERSION)
        .setSchemaMinorVersion(SchemaInformation.SCHEMA_MAJOR_VERSION);
  }

  public static DatasetProfile fromProtobuf(DatasetProfileMessage message) {
    val props = message.getProperties();
    val tags = Lists.transform(props.getTagsList().asByteStringList(), ByteString::toStringUtf8);
    val sessionTimestamp = Instant.ofEpochMilli(props.getSessionTimestamp());
    val dataTimestamp =
        (props.getDataTimestamp() < 0L) ? null : Instant.ofEpochMilli(props.getDataTimestamp());
    val ds =
        new DatasetProfile(
            props.getSessionId(), sessionTimestamp, dataTimestamp, tags, Collections.emptyMap());
    ds.withAllMetadata(props.getMetadataMap());
    message.getColumnsMap().forEach((k, v) -> ds.columns.put(k, ColumnProfile.fromProtobuf(v)));

    ds.validate();

    return ds;
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    validate();

    toProtobuf().build().writeTo(out);
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    val msg = DatasetProfileMessage.parseFrom(in);

    final DatasetProfile copy = fromProtobuf(msg);
    this.sessionId = copy.sessionId;
    this.sessionTimestamp = copy.sessionTimestamp;
    this.dataTimestamp = copy.dataTimestamp;
    this.metadata = copy.metadata;
    this.tags = copy.tags;
    this.columns = copy.columns;

    this.validate();
  }

  @Value
  static class Pair {
    String name;
    ColumnSummary statistics;

    static Pair fromColumn(ColumnProfile column) {
      return new Pair(column.getColumnName(), column.toColumnSummary());
    }
  }
}
