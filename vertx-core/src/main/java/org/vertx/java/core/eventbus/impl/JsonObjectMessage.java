/*
 * Copyright 2011-2012 the original author or authors.
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

package org.vertx.java.core.eventbus.impl;

import io.netty.util.CharsetUtil;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class JsonObjectMessage extends BaseMessage<JsonObject> {

  private static final Logger log = LoggerFactory.getLogger(JsonObjectMessage.class);

  private byte[] encoded;

  public JsonObjectMessage(boolean send, String address, JsonObject body) {
    super(send, address, body);
  }

  private JsonObjectMessage(JsonObjectMessage other) {
    super(other.send, other.address, other.body == null ? null : other.body.copy());
    this.replyAddress = other.replyAddress;
    this.bus = other.bus;
    this.sender = other.sender;
  }

  public JsonObjectMessage(Buffer readBuff) {
    super(readBuff);
  }

  @Override
  protected void readBody(int pos, Buffer readBuff) {
    boolean isNull = readBuff.getByte(pos) == (byte)0;
    if (!isNull) {
      pos++;
      int strLength = readBuff.getInt(pos);
      pos += 4;
      byte[] bytes = readBuff.getBytes(pos, pos + strLength);
      String str = new String(bytes, CharsetUtil.UTF_8);
      body = new JsonObject(str);
    }
  }

  @Override
  protected void writeBody(Buffer buff) {
    if (body == null) {
      buff.appendByte((byte)0);
    } else {
      buff.appendByte((byte)1);
      buff.appendInt(encoded.length);
      buff.appendBytes(encoded);
    }
  }

  @Override
  protected int getBodyLength() {
    if (body == null) {
      return 1;
    } else {
      String strJson = body.encode();
      encoded = strJson.getBytes(CharsetUtil.UTF_8);
      return 1 + 4 + encoded.length;
    }
  }

  @Override
  protected Message<JsonObject> copy() {
    return new JsonObjectMessage(this);
  }

  @Override
  protected byte type() {
    return MessageFactory.TYPE_JSON;
  }

}
