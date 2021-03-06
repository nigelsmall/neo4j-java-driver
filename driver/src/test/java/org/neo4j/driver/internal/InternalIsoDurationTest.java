/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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
package org.neo4j.driver.internal;

import org.junit.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.Temporal;
import java.time.temporal.UnsupportedTemporalTypeException;

import org.neo4j.driver.v1.types.IsoDuration;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.time.temporal.ChronoUnit.NANOS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.time.temporal.ChronoUnit.YEARS;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class InternalIsoDurationTest
{
    @Test
    public void shouldExposeMonths()
    {
        IsoDuration duration = newDuration( 42, 1, 2, 3 );
        assertEquals( 42, duration.months() );
        assertEquals( 42, duration.get( MONTHS ) );
    }

    @Test
    public void shouldExposeDays()
    {
        IsoDuration duration = newDuration( 1, 42, 2, 3 );
        assertEquals( 42, duration.days() );
        assertEquals( 42, duration.get( DAYS ) );
    }

    @Test
    public void shouldExposeSeconds()
    {
        IsoDuration duration = newDuration( 1, 2, 42, 3 );
        assertEquals( 42, duration.seconds() );
        assertEquals( 42, duration.get( SECONDS ) );
    }

    @Test
    public void shouldExposeNanoseconds()
    {
        IsoDuration duration = newDuration( 1, 2, 3, 42 );
        assertEquals( 42, duration.nanoseconds() );
        assertEquals( 42, duration.get( NANOS ) );
    }

    @Test
    public void shouldFailToGetUnsupportedTemporalUnit()
    {
        IsoDuration duration = newDuration( 1, 2, 3, 4 );

        try
        {
            duration.get( YEARS );
            fail( "Exception expected" );
        }
        catch ( UnsupportedTemporalTypeException ignore )
        {
        }
    }

    @Test
    public void shouldExposeSupportedTemporalUnits()
    {
        IsoDuration duration = newDuration( 1, 2, 3, 4 );
        assertEquals( asList( MONTHS, DAYS, SECONDS, NANOS ), duration.getUnits() );
    }

    @Test
    public void shouldAddTo()
    {
        IsoDuration duration = newDuration( 1, 2, 3, 4 );
        LocalDateTime dateTime = LocalDateTime.of( 1990, 1, 1, 0, 0, 0, 0 );

        Temporal result = duration.addTo( dateTime );

        assertEquals( LocalDateTime.of( 1990, 2, 3, 0, 0, 3, 4 ), result );
    }

    @Test
    public void shouldSubtractFrom()
    {
        IsoDuration duration = newDuration( 4, 3, 2, 1 );
        LocalDateTime dateTime = LocalDateTime.of( 1990, 7, 19, 0, 0, 59, 999 );

        Temporal result = duration.subtractFrom( dateTime );

        assertEquals( LocalDateTime.of( 1990, 3, 16, 0, 0, 57, 998 ), result );
    }

    @Test
    public void shouldImplementEqualsAndHashCode()
    {
        IsoDuration duration1 = newDuration( 1, 2, 3, 4 );
        IsoDuration duration2 = newDuration( 1, 2, 3, 4 );

        assertEquals( duration1, duration2 );
        assertEquals( duration1.hashCode(), duration2.hashCode() );
    }

    @Test
    public void shouldCreateFromPeriod()
    {
        Period period = Period.of( 3, 5, 12 );

        InternalIsoDuration duration = new InternalIsoDuration( period );

        assertEquals( period.toTotalMonths(), duration.months() );
        assertEquals( period.getDays(), duration.days() );
        assertEquals( 0, duration.seconds() );
        assertEquals( 0, duration.nanoseconds() );
    }

    @Test
    public void shouldCreateFromDuration()
    {
        Duration duration = Duration.ofSeconds( 391784, 4879173 );

        InternalIsoDuration isoDuration = new InternalIsoDuration( duration );

        assertEquals( 0, isoDuration.months() );
        assertEquals( 0, isoDuration.days() );
        assertEquals( duration.getSeconds(), isoDuration.seconds() );
        assertEquals( duration.getNano(), isoDuration.nanoseconds() );
    }

    @Test
    public void toStringShouldPrintInIsoStandardFormat() throws Throwable
    {
        assertThat( new InternalIsoDuration( 0, 0, 0, 0 ).toString(), equalTo( "PT0S" ) );
        assertThat( new InternalIsoDuration( Period.parse( "P356D" ) ).toString(), equalTo( "P50W6D" ) );
        assertThat( new InternalIsoDuration( Duration.parse( "PT45S" ) ).toString(), equalTo( "PT45S" ) );

        assertThat( new InternalIsoDuration( Period.parse( "P14D" ), Duration.parse( "PT16H12M" ) ).toString(), equalTo( "P2WT16H12M" ) );
        assertThat( new InternalIsoDuration( Period.parse( "P5M1D" ), Duration.parse( "PT12H" ) ).toString(), equalTo( "P5M1DT12H" ) );
        assertThat( new InternalIsoDuration( Period.parse( "P2W3D" ), Duration.parse( "PT12H" ) ).toString(), equalTo( "P2W3DT12H" ) );
    }

    private static IsoDuration newDuration( long months, long days, long seconds, int nanoseconds )
    {
        return new InternalIsoDuration( months, days, seconds, nanoseconds );
    }
}
