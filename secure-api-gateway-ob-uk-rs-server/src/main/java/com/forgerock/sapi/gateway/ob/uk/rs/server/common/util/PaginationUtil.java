/*
 * Copyright © 2020-2025 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.sapi.gateway.ob.uk.rs.server.common.util;

import org.springframework.web.util.UriComponentsBuilder;
import uk.org.openbanking.datamodel.v3.common.Links;
import uk.org.openbanking.datamodel.v3.common.Meta;

import java.net.URI;

public class PaginationUtil {

    public static final String PAGE = "page";

    public static Links generateLinks(String httpUrl, int page, int totalPages) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
                .fromHttpUrl(httpUrl);
        return generateLinks(uriComponentsBuilder, page, totalPages);
    }

    public static Links generateLinks(UriComponentsBuilder uriComponentsBuilder, int page, int totalPages) {
        Links links = new Links();

        URI resourceURI = uriComponentsBuilder.build().encode().toUri();
        if (isFirstPage(page)) {
            links.setSelf(resourceURI);
            if (totalPages > 1) {
                links.setFirst(getUrlWithPage(0, uriComponentsBuilder));
                //no previous
                links.setNext(getUrlWithPage(page + 1, uriComponentsBuilder));
                links.setLast(getUrlWithPage(totalPages - 1, uriComponentsBuilder));
            }
        } else if (isLastPage(page, totalPages)) {
            links.setSelf(getUrlWithPage(page, uriComponentsBuilder));
            links.setFirst(getUrlWithPage(0, uriComponentsBuilder));
            links.setPrev(getUrlWithPage(page - 1, uriComponentsBuilder));
            //No next
            links.setLast(getUrlWithPage(totalPages - 1, uriComponentsBuilder));
        } else {
            links.setSelf(getUrlWithPage(page, uriComponentsBuilder));
            links.setFirst(getUrlWithPage(0, uriComponentsBuilder));
            links.setPrev(getUrlWithPage(page - 1, uriComponentsBuilder));
            links.setNext(getUrlWithPage(page + 1, uriComponentsBuilder));
            links.setLast(getUrlWithPage(totalPages - 1, uriComponentsBuilder));
        }
        return links;
    }

    public static Links generateLinksOnePager(String httpUrl) {
        URI resourceUrl = UriComponentsBuilder.fromHttpUrl(httpUrl).build().encode().toUri();

        Links links = new Links();
        links.setSelf(resourceUrl);
        return links;
    }

    public static Meta generateMetaData(int totalPages) {
        return new Meta().totalPages(totalPages);
    }

    private static URI getUrlWithPage(int page, UriComponentsBuilder builder) {
        return builder.replaceQueryParam(PAGE, page).build().encode().toUri();
    }

    private static boolean isLastPage(int page, int totalPages) {
        return page == totalPages - 1;
    }

    private static boolean isFirstPage(int page) {
        return page == 0;
    }

}
