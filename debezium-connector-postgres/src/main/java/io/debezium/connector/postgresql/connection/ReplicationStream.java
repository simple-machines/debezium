/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */

package io.debezium.connector.postgresql.connection;

import java.sql.SQLException;
import org.postgresql.replication.PGReplicationStream;

import io.debezium.connector.postgresql.proto.PgProto;

/**
 * A stream from which messages sent by a logical decoding plugin can be consumed over a replication connection.
 * 
 * @author Horia Chiorean (hchiorea@redhat.com)
 */
public interface ReplicationStream extends AutoCloseable {
    
    /**
     * Blocks and waits for a Protobuf message to be sent over a replication connection. Once a message has been received, 
     * the value of the {@link #lastReceivedLSN() last received LSN} will also be updated accordingly.
     *
     * @return a {@link io.debezium.connector.postgresql.proto.PgProto.RowMessage} instance; this may return {@code null} if
     * the server sends back a message which has already been reported as consumed via the {@link #flushLSN()} method.
     * @throws SQLException if anything unexpected fails
     * @see PGReplicationStream#read() 
     */
    PgProto.RowMessage read() throws SQLException;
    
    /**
     * Attempts to read a Protobuf message from a replication connection, returning that message if it's available or returning
     * {@code null} if nothing is available. Once a message has been received, the value of the {@link #lastReceivedLSN() last received LSN} 
     * will also be updated accordingly.
     *
     * @return a {@link io.debezium.connector.postgresql.proto.PgProto.RowMessage} instance if a message is available and was
     * written by a server or {@code null} if nothing is available from the server or the server sends a message that has
     * already been reported as consumed via the {@link #flushLSN()} method.
     * @throws SQLException if anything unexpected fails
     * @see PGReplicationStream#readPending() 
     */
    PgProto.RowMessage readPending() throws SQLException;
    
    /**
     * Sends a message to the server informing it about that latest position in the WAL that this stream has read via
     * {@link ReplicationConnection#startStreaming()} or {@link ReplicationConnection#startStreaming(Long)}.
     * <p>
     * This essentially tells the server that this stream has successfully processed messages up to the current read cursor
     * and so the server is free to discard older segments with earlier LSNs. It also affects the catch-up behavior once a slot
     * is restarted and the server attempt to bring it up-to-date.
     * </p>
     *
     * @throws SQLException if anything goes wrong
     */
    void flushLSN() throws SQLException;
    
    /**
     * Returns the value for the latest server received LSN during a read operation. The value is always updated once messages
     * are read via the {@link ReplicationConnection#startStreaming()} or {@link ReplicationConnection#startStreaming(Long)} 
     * methods.
     *
     * @return a {@link Long} value, possibly null if this is called before anything has been read
     */
    Long lastReceivedLSN();
    
    /**
     * //TODO author=Horia Chiorean date=13/10/2016 description=Don't use this for now, because of the bug from the PG server
     * This is stream is closed atm. once the replication connection which created it is closed.
     * @see PGReplicationStream#close() 
     */
    @Override
    void close() throws Exception;
}
