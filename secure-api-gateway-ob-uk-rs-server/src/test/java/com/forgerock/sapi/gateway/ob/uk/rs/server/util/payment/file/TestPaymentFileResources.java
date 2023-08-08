/*
 * Copyright © 2020-2022 ForgeRock AS (obst@forgerock.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.forgerock.sapi.gateway.ob.uk.rs.server.util.payment.file;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.DefaultPaymentFileType;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.PaymentFile;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.PaymentFileType;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.HashUtils;

/**
 * Class containing test payment files and their metadata
 */
public class TestPaymentFileResources {

    public static final String PAIN_001_001_08_FILE_PATH = "src/test/resources/payment/files/UK_OBIE_pain_001_001_08.xml";

    public static final String PAYMENT_INITIATION_3_1_FILE_PATH = "src/test/resources/payment/files/UK_OBIE_PaymentInitiation_3_1.json";

    private static final TestPaymentFileResources INSTANCE = new TestPaymentFileResources();

    public static TestPaymentFileResources getInstance() {
        return INSTANCE;
    }

    private final Map<String, TestPaymentFile> paymentFiles;

    private TestPaymentFileResources() {
        paymentFiles = new HashMap<>();
        paymentFiles.put(PAIN_001_001_08_FILE_PATH, loadTestPaymentFile(PAIN_001_001_08_FILE_PATH,
                DefaultPaymentFileType.UK_OBIE_PAIN_001.getPaymentFileType(), 3, new BigDecimal("11500000")));

        paymentFiles.put(PAYMENT_INITIATION_3_1_FILE_PATH, loadTestPaymentFile(PAYMENT_INITIATION_3_1_FILE_PATH,
                DefaultPaymentFileType.UK_OBIE_PAYMENT_INITIATION_V3_1.getPaymentFileType(), 4, new BigDecimal("87")));
    }

    public Map<String, TestPaymentFile> getPaymentFiles() {
        return paymentFiles;
    }

    public TestPaymentFile getPaymentFile(String filePath) {
        final TestPaymentFile testPaymentFile = paymentFiles.get(filePath);
        if (testPaymentFile == null) {
            throw new IllegalStateException("Test file configuration not found for path: " + filePath);
        }
        return testPaymentFile;
    }

    private TestPaymentFile loadTestPaymentFile(String path, PaymentFileType fileType, int numTransactions, BigDecimal controlSum) {
        try {
            final String fileContent = Files.readString(new File(path).toPath());
            final String fileHash = HashUtils.computeSHA256FullHash(fileContent);
            return new TestPaymentFile(fileContent, fileType, fileHash, numTransactions, controlSum);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class TestPaymentFile {
        private final String fileContent;
        private final PaymentFileType fileType;
        private final String fileHash;
        private final int numTransactions;
        private final BigDecimal controlSum;

        public TestPaymentFile(String fileContent, PaymentFileType fileType, String fileHash, int numTransactions, BigDecimal controlSum) {
            this.fileContent = fileContent;
            this.fileType = fileType;
            this.fileHash = fileHash;
            this.numTransactions = numTransactions;
            this.controlSum = controlSum;
        }

        public String getFileContent() {
            return fileContent;
        }

        public PaymentFileType getFileType() {
            return fileType;
        }

        public BigDecimal getControlSum() {
            return controlSum;
        }

        public int getNumTransactions() {
            return numTransactions;
        }

        public String getFileHash() {
            return fileHash;
        }

        public PaymentFile toPaymentFile() {
            return new PaymentFile(numTransactions, controlSum, fileType, null);
        }
    }

}
