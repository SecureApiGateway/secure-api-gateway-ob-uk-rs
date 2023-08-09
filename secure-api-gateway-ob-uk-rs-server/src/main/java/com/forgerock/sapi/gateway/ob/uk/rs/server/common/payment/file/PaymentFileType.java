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
package com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file;

import java.util.Objects;

import org.springframework.http.MediaType;

public class PaymentFileType {

    private final String fileType;
    private final MediaType contentType;

    public PaymentFileType(String fileType, MediaType contentType) {
        this.fileType = Objects.requireNonNull(fileType, "fileType must be supplied");
        this.contentType = Objects.requireNonNull(contentType, "contentType must be supplied");
    }

    public MediaType getContentType() {
        return contentType;
    }

    public String getFileType() {
        return fileType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PaymentFileType)) return false;
        final PaymentFileType that = (PaymentFileType) o;
        return Objects.equals(getFileType(), that.getFileType()) && Objects.equals(getContentType(), that.getContentType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFileType(), getContentType());
    }

    @Override
    public String toString() {
        return "PaymentFileType{" +
                "fileType='" + fileType + '\'' +
                ", contentType=" + contentType +
                '}';
    }
}
