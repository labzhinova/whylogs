package com.whylabs.logging.firehose;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.kinesisfirehose.AmazonKinesisFirehose;
import com.amazonaws.services.kinesisfirehose.AmazonKinesisFirehoseClient;
import com.amazonaws.services.kinesisfirehose.model.PutRecordRequest;
import com.amazonaws.services.kinesisfirehose.model.Record;
import com.whylabs.logging.core.DatasetProfile;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class FirehosePublisher {
  private final AmazonKinesisFirehose firehoseClient;

  public FirehosePublisher() {
    this.firehoseClient =
        AmazonKinesisFirehoseClient.builder()
            .withRegion("us-west-2")
            .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
            .build();
  }

  public void putData(final DatasetProfile profile) {
    val intpObj = profile.toSummary();

    val putRequest =
        new PutRecordRequest()
            .withRecord(new Record().withData(null))
            .withDeliveryStreamName("andy-test-data");
    firehoseClient.putRecord(putRequest);
  }
}
