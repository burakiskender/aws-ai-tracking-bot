/*
 Copyright 2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.

 Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the
 License. A copy of the License is located at

 http://aws.amazon.com/apache2.0/

 or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 and limitations under the License.
 */

package com.amazonaws.mobile;

import android.content.Context;
import android.util.Log;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobilehelper.auth.IdentityManager;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

/**
 * The AWS Mobile Client bootstraps the application to make calls to AWS 
 * services. It creates clients which can be used to call services backing the
 * features you selected in your project.
 */
public class AWSMobileClient {

    private static final String LOG_TAG = AWSMobileClient.class.getSimpleName();

    private static volatile AWSMobileClient instance;

    private ClientConfiguration clientConfiguration;
    private IdentityManager identityManager;

    private AmazonDynamoDBClient dynamoDBClient;
    private DynamoDBMapper dynamoDBMapper;

    /**
     * Build class used to create the AWS mobile client.
     */
    public static class Builder {

        private Context applicationContext;
        private String  cognitoIdentityPoolID;
        private Regions cognitoRegion;
        private ClientConfiguration clientConfiguration;
        private IdentityManager identityManager;

	/**
	 * Constructor.
	 * @param context Android context.
	 */
        public Builder(final Context context) {
            this.applicationContext = context.getApplicationContext();
        };

	/**
	 * Provides the Amazon Cognito Identity Pool ID.
	 * @param cognitoIdentityPoolID identity pool ID
	 * @return builder
	 */
        public Builder withCognitoIdentityPoolID(final String cognitoIdentityPoolID) {
            this.cognitoIdentityPoolID = cognitoIdentityPoolID;
            return this;
        };
        
	/**
	 * Provides the Amazon Cognito service region.
	 * @param cognitoRegion service region
	 * @return builder
	 */
        public Builder withCognitoRegion(final Regions cognitoRegion) {
            this.cognitoRegion = cognitoRegion;
            return this;
        }

        /**
         * Provides the identity manager.
	 * @param identityManager identity manager
	 * @return builder
	 */
        public Builder withIdentityManager(final IdentityManager identityManager) {
            this.identityManager = identityManager;
            return this;
        }

        /**
         * Provides the client configuration
         * @param clientConfiguration client configuration
         * @return builder
         */
        public Builder withClientConfiguration(final ClientConfiguration clientConfiguration) {
            this.clientConfiguration = clientConfiguration;
            return this;
        }

	/**
	 * Creates the AWS mobile client instance and initializes it.
	 * @return AWS mobile client
	 */
        public AWSMobileClient build() {
            return
                new AWSMobileClient(applicationContext,
                                    cognitoIdentityPoolID,
                                    cognitoRegion,
                                    identityManager,
                                    clientConfiguration);
        }
    }

    private AWSMobileClient(final Context context,
                            final String  cognitoIdentityPoolID,
                            final Regions cognitoRegion,
                            final IdentityManager identityManager,
                            final ClientConfiguration clientConfiguration) {
        AWSConfiguration awsConfiguration = AWSConfiguration.getInstance(context.getResources());
        this.identityManager = identityManager;
        this.clientConfiguration = clientConfiguration;
        this.dynamoDBClient = new AmazonDynamoDBClient(identityManager.getCredentialsProvider(), clientConfiguration);
        this.dynamoDBClient.setRegion(Region.getRegion(awsConfiguration.getAMAZON_DYNAMODB_REGION()));
        this.dynamoDBMapper = new DynamoDBMapper(dynamoDBClient);

    }

    /**
     * Sets the singleton instance of the AWS mobile client.
     * @param client client instance
     */
    public static void setDefaultMobileClient(AWSMobileClient client) {
        instance = client;
    }

    /**
     * Gets the default singleton instance of the AWS mobile client.
     * @return client
     */
    public static AWSMobileClient defaultMobileClient() {
        return instance;
    }

    /**
     * Gets the identity manager.
     * @return identity manager
     */
    public IdentityManager getIdentityManager() {
        return this.identityManager;
    }


    private static void addSignInProviders(final Context context, final IdentityManager identityManager) {
    }

    /**
     * Creates and initialize the default AWSMobileClient if it doesn't already
     * exist using configuration constants from {@link AWSConfiguration}.
     *
     * @param context an application context.
     */
    public static void initializeMobileClientIfNecessary(final Context context) {
        AWSConfiguration awsConfiguration = AWSConfiguration.getInstance(context.getResources());
        if (AWSMobileClient.defaultMobileClient() == null) {
            Log.d(LOG_TAG, "Initializing AWS Mobile Client...");
            final ClientConfiguration clientConfiguration = new ClientConfiguration();
            clientConfiguration.setUserAgent(awsConfiguration.getAWS_MOBILEHUB_USER_AGENT());
            final IdentityManager identityManager = new IdentityManager(context, clientConfiguration,
                awsConfiguration.getAWSMobileHelperConfiguration());

            addSignInProviders(context, identityManager);

            final AWSMobileClient awsClient =
                new AWSMobileClient.Builder(context)
                    .withCognitoRegion(awsConfiguration.getAMAZON_COGNITO_REGION())
                    .withCognitoIdentityPoolID(awsConfiguration.getAMAZON_COGNITO_IDENTITY_POOL_ID())
                    .withIdentityManager(identityManager)
                    .withClientConfiguration(clientConfiguration)
                    .build();

            AWSMobileClient.setDefaultMobileClient(awsClient);
        }
        Log.d(LOG_TAG, "AWS Mobile Client is OK");
    }

    /**
     * Gets the DynamoDB Client, which allows accessing Amazon DynamoDB tables.
     * @return the DynamoDB client instance.
     */
    public AmazonDynamoDBClient getDynamoDBClient() {
        return dynamoDBClient;
    }

    /**
     * Gets the Dynamo DB Object Mapper, which allows accessing DynamoDB tables using annotated
     * data object classes to represent your data using POJOs (Plain Old Java Objects).
     * @return the DynamoDB Object Mapper instance.
     */
    public DynamoDBMapper getDynamoDBMapper() {
        return dynamoDBMapper;
    }


}
