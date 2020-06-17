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

import java.lang.Object;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.Collectors;

public final class FindMeetingQuery {
  /**
  * Find all possible time ranges for a meeting. If possible find ranges where optional attendees 
  * can also participate. If no such ranges are found, ignore optional attendees. 
  * 
  * @param events  a collection of all events that may be relevant
  * @param request the meeting request that specifies optional and mandatory guests for the event 
  *                as well as the duration of the event
  * @return        A collection of all possible slots for the meeting. This means all the ranges 
  *                during which all guests are available. 
  */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    if (!request.getOptionalAttendees().isEmpty()) {
      Collection<TimeRange> ranges = 
          findAvailableTimeSlots(events, 
                                 mergeCollections(request.getAttendees(), 
                                                  request.getOptionalAttendees()), 
                                 request.getDuration());

      if (!ranges.isEmpty()) {
        return ranges;
      } 
      
      if (request.getAttendees().isEmpty()) {
        // Tests specify that in this case (where there are no mandatory attendees and we can't 
        // find a slot for the optional attendees) we should return no slots rather than the whole 
        // day.
        return Collections.emptyList(); 
      }
    }

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
    
    Stream<Event> relevantEvents = events.stream().filter(event -> hasAttendee(event, attendees));
    Stream<TimeRange> eventTimes = relevantEvents.map(event -> event.getWhen());
    
    Collection<TimeRange> potentialSlots = filterTooShort(getInbetweenRanges(eventTimes), duration);

    return potentialSlots;
  }

  private Collection<String> mergeCollections(Collection<String> collectionA, 
                                             Collection<String> collectionB) {
    return Stream.concat(collectionA.stream(), collectionB.stream())
                 .collect(Collectors.toList());
  }

  /* Check if at least one of the attendees is attending the event */
  private Boolean hasAttendee(Event event, Collection<String> attendees) {
    return attendees.stream().anyMatch(event.getAttendees()::contains);
  }

  /**
   * Remove timeranges that are nested in other timeranges. A timerange A is nested in timerange B if 
   * B starts earlier than A abd ends later than A. Like so:       
   *                    |--A--|
   *                  |----B----|
   *
   * Precondition: ranges is sorted by starting time.
   */
  private List<TimeRange> filterOutNestedTimeRanges(List<TimeRange> ranges) 
                                            throws IllegalArgumentException {
                                        
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
   * Preconditions: - ranges is sorted by starting time 
   */
  private Collection<TimeRange> getInbetweenRanges(Stream<TimeRange> ranges) 
                                                  throws IllegalArgumentException {
    List<TimeRange> rangeList = filterOutNestedTimeRanges(ranges.sorted(TimeRange.ORDER_BY_START)
                                                                .collect(Collectors.toList()));                                              
    if (rangeList.isEmpty()) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    } 

    Collection<TimeRange> inbetweens = new ArrayList<TimeRange>();

    // add range for time before first event starts
    inbetweens.add(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, rangeList.get(0).start(), false));


    for (int i = 0; i < rangeList.size() - 1; i++) {
      TimeRange a = rangeList.get(i);
      TimeRange b = rangeList.get(i + 1);

      inbetweens.add(TimeRange.fromStartEnd(a.end(), b.start(), false));  
    }

    // add range for time after last event ends
    inbetweens.add(TimeRange.fromStartEnd(rangeList.get(rangeList.size() - 1).end(), 
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
