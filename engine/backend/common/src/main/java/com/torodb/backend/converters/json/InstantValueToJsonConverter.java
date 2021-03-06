/*
 * ToroDB
 * Copyright © 2014 8Kdata Technology (www.8kdata.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.torodb.backend.converters.json;

import com.torodb.backend.converters.ValueConverter;
import com.torodb.kvdocument.values.KvInstant;
import com.torodb.kvdocument.values.heap.InstantKvInstant;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 *
 */
public class InstantValueToJsonConverter implements
    ValueConverter<String, KvInstant> {

  private static final long serialVersionUID = 1L;

  @Override
  public Class<? extends String> getJsonClass() {
    return String.class;
  }

  @Override
  public Class<? extends KvInstant> getValueClass() {
    return KvInstant.class;
  }

  @Override
  public KvInstant toValue(String value) {
    return new InstantKvInstant(Instant.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(value)));
  }
}
