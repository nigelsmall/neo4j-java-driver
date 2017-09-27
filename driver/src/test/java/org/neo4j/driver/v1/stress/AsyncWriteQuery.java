/*
 * Copyright (c) 2002-2017 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
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
package org.neo4j.driver.v1.stress;

import java.util.concurrent.CompletionStage;

import org.neo4j.driver.v1.AccessMode;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResultCursor;

import static org.junit.Assert.assertEquals;

public class AsyncWriteQuery<C extends AbstractContext> extends AbstractAsyncQuery<C>
{
    private AbstractStressIT<C> abstractStressIT;

    public AsyncWriteQuery( AbstractStressIT<C> abstractStressIT, Driver driver, boolean useBookmark )
    {
        super( driver, useBookmark );
        this.abstractStressIT = abstractStressIT;
    }

    @Override
    public CompletionStage<Void> execute( C context )
    {
        Session session = newSession( AccessMode.WRITE, context );

        return session.runAsync( "CREATE ()" )
                .thenCompose( StatementResultCursor::summaryAsync )
                .handle( ( summary, error ) ->
                {
                    session.closeAsync();

                    handleError( error, context );
                    assertEquals( 1, summary.counters().nodesCreated() );
                    context.nodeCreated();
                    return null;
                } );
    }

    private void handleError( Throwable error, C context )
    {
        if ( error != null )
        {
            if ( !abstractStressIT.handleWriteFailure( error, context ) )
            {
                throw new RuntimeException( error );
            }
        }
    }
}