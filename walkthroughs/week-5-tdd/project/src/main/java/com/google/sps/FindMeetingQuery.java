// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.Collectors;

public final class FindMeetingQuery {
  /* Find all possible time ranges for a meeting */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    if (!request.getOptionalAttendees().isEmpty()) {
      return queryWithOptionalAttendees(events, request);
    } else {
      return queryWithoutOptionalAttendees(events, request);
    }
  }

  /** Run a query with optional attendees. First try to find slots that work everyone, including 
    * the optional attendees. If that fails ignore the optional attendees and find slots that work
    * for the mandatory ones.
    */
  private Collection<TimeRange> queryWithOptionalAttendees(Collection<Event> events, 
                                                           MeetingRequest request) {
    Collection<TimeRange> ranges = queryWithOptionalAttendeesAsMandatory(events, request);

    if (!ranges.isEmpty()) {
      return ranges;
    } 
    
    if (request.getAttendees().isEmpty()) {
      // Tests specify that in this case (where there are no mandatory attendees and we can't find 
      // a slot for the optional attendees) we should return no slots rather than the whole day.
      return Collections.emptyList(); 
    }

    return queryWithoutOptionalAttendees(events, request);
  }

  private Collection<TimeRange> queryWithoutOptionalAttendees(Collection<Event> events,         
                                                              MeetingRequest request) {
    return findAvailableTimeSlots(events, request.getAttendees(), request.getDuration());
  }

  private Collection<TimeRange> queryWithOptionalAttendeesAsMandatory(Collection<Event> events, 
                                                                      MeetingRequest request) {
    Collection<String> allAttendees = mergeCollections(request.getAttendees(), 
                                                       request.getOptionalAttendees());

    return findAvailableTimeSlots(events, allAttendees, request.getDuration());
  }

  private Collection<TimeRange> findAvailableTimeSlots(Collection<Event> events, 
                                                       Collection<String> attendees, 
                                                       long duration) {
    Collection<Event> relevantEvents = filterUnattendedEvents(events, attendees);
    
    List<TimeRange> eventTimes = 
                          filterNestedTimeRanges(sortByStart(extractTimeRanges(relevantEvents)));
    
    Collection<TimeRange> inbetweenRanges = getInbetweenRanges(eventTimes);

    return filterTooShort(inbetweenRanges, duration);
  }

  private Collection<String> mergeCollections(Collection<String> collectionA, 
                                             Collection<String> collectionB) {
    return Stream.concat(collectionA.stream(), collectionB.stream())
                .collect(Collectors.toList());
  }

  /* Filter out events that noone is attending */
  private Collection<Event> filterUnattendedEvents(Collection<Event> events, 
                                                   Collection<String> attendees) {    
    return events.stream()
                 .filter(event -> hasAttendee(event, attendees))
                 .collect(Collectors.toList());
  }

  /* Check if at least one of the attendees is attending the event */
  private Boolean hasAttendee(Event event, Collection<String> attendees) {
    return attendees.stream().anyMatch(event.getAttendees()::contains);
  } 

  private List<TimeRange> extractTimeRanges(Collection<Event> events) {
    return events.stream()
                 .map(event -> event.getWhen())
                 .collect(Collectors.toList());
  }

  private List<TimeRange> sortByStart(List<TimeRange> ranges) {
    Collections.sort(ranges, TimeRange.ORDER_BY_START);

    return ranges;
  }

  /**
   * Remove timeranges that are nested in other timeranges. A timerange A is nested in timerange B if 
   * B starts earlier than A abd ends later than A. Like so:       
   *                    |--A--|
   *                  |----B----|
   *
   * Precondition: ranges is sorted by starting time.
   */
  private List<TimeRange> filterNestedTimeRanges(List<TimeRange> ranges) {
    int i = 0;
    while (i + 1 < ranges.size()) {
      if (ranges.get(i).contains(ranges.get(i + 1))) {
        ranges.remove(i + 1);
      } else {
        i++;
      }
    }

    return ranges;
  }

  /**
   * Find ranges between consecutive ranges. 
   * Input       |--A--|   |--B--|         |---C---|
   * Return  |-1-|     |-2-|     |----3----|       |---4---|
   * 
   * Preconditions: ranges is sorted by starting time and there are no nested events.
   */
  private Collection<TimeRange> getInbetweenRanges(List<TimeRange> ranges) {
    if (ranges.isEmpty()) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    } 
    Collection<TimeRange> inbetweens = new ArrayList<TimeRange>();

    // add range for time before first event starts
    inbetweens.add(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, ranges.get(0).start(), false));


    for (int i = 0; i < ranges.size() - 1; i++) {
      TimeRange a = ranges.get(i);
      TimeRange b = ranges.get(i + 1);

      inbetweens.add(TimeRange.fromStartEnd(a.end(), b.start(), false));  
    }

    // add range for time after last event ends
    inbetweens.add(TimeRange.fromStartEnd(ranges.get(ranges.size() - 1).end(), 
                                          TimeRange.END_OF_DAY, 
                                          true));

    return inbetweens;
  }

  private Collection<TimeRange> filterTooShort(Collection<TimeRange> ranges, long minDuration) {
    return ranges.stream()
                 .filter(range -> range.duration() >= minDuration)
                 .collect(Collectors.toList());
  }
}
