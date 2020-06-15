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
      return queryWithOptional(events, request);
    } else {
      return queryNoOptional(events, request);
    }
  }

  private Collection<TimeRange> queryWithOptional(Collection<Event> events, MeetingRequest request) {
    Collection<TimeRange> ranges = queryOptionalMandatory(events, request);

    if (ranges.isEmpty()) {
      if (request.getAttendees().isEmpty()) {
        // Tests specify that in this case we should return no slots rather than the whole day.
        return ranges; 
      }
      return queryOptionalRemoved(events, request);
    } else {
      return ranges;
    }
  }

  private Collection<TimeRange> queryNoOptional(Collection<Event> events, MeetingRequest request) {
    Collection<Event> relevantEvents = filterRelevant(events, request.getAttendees());
    List<TimeRange> eventTimes = filterNested(sortByStart(collectTimeRanges(relevantEvents)));
    Collection<TimeRange> inbetweenRanges = getInbetweenRanges(eventTimes);

    return filterTooShort(inbetweenRanges, request.getDuration());
  }

  private Collection<TimeRange> queryOptionalMandatory(Collection<Event> events, MeetingRequest request) {
    Collection<String> allAttendees = mergeCollection(request.getAttendees(), request.getOptionalAttendees());
    MeetingRequest newRequest = new MeetingRequest(allAttendees, request.getDuration());

    return query(events, newRequest);
  }

  private Collection<String> mergeCollection(Collection<String> collectionA, Collection<String> collectionB) {
    return Stream.concat(collectionA.stream(), collectionB.stream())
                .collect(Collectors.toList());
  }

  private Collection<TimeRange> queryOptionalRemoved(Collection<Event> events, MeetingRequest request) {
    return query(events, new MeetingRequest(request.getAttendees(), request.getDuration()));
  }


  /* Filter out events that noone is attending */
  private Collection<Event> filterRelevant(Collection<Event> events, Collection<String> attendees) {    
    return events.stream()
                 .filter(event -> someoneAttending(event, attendees))
                 .collect(Collectors.toList());
  }

  /* Check if at least one of the attendees is attending the event */
  private Boolean someoneAttending(Event event, Collection<String> attendees) {
    return attendees.stream().anyMatch(event.getAttendees()::contains);
  } 

  private List<TimeRange> collectTimeRanges(Collection<Event> events) {
    return events.stream()
                 .map(event -> event.getWhen())
                 .collect(Collectors.toList());
  }

  private List<TimeRange> sortByStart(List<TimeRange> ranges) {
    Collections.sort(ranges, TimeRange.ORDER_BY_START);

    return ranges;
  }

  /**
   * Remove timeranges that are nested in other timeranges
   * Like so  :       |----A----|
   *                    |--B--|
   * Precondition: ranges is sorted by starting time.
   */
  private List<TimeRange> filterNested(List<TimeRange> ranges) {
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
    } else {
      Collection<TimeRange> inbetweens = new ArrayList<TimeRange>();

      TimeRange a;
      TimeRange b;
      TimeRange inbetween;

      // add range for time before first event starts
      inbetween = TimeRange.fromStartEnd(TimeRange.START_OF_DAY, ranges.get(0).start(), false);
      inbetweens.add(inbetween);

      for (int i = 0; i < ranges.size() - 1; i++) {
        a = ranges.get(i);
        b = ranges.get(i+1);

        inbetween = TimeRange.fromStartEnd(a.end(), b.start(), false);
        inbetweens.add(inbetween);  
      }

      // add range for time after last event ends
      inbetween = TimeRange.fromStartEnd(lastElem(ranges).end(), TimeRange.END_OF_DAY, true);
      inbetweens.add(inbetween);

      return inbetweens;
    }
  }

  private Collection<TimeRange> filterTooShort(Collection<TimeRange> ranges, long minDuration) {
    return ranges.stream()
                 .filter(range -> range.duration() >= minDuration)
                 .collect(Collectors.toList());
  }

  private TimeRange lastElem(List<TimeRange> list) {
    return list.get(list.size() - 1);
  }

}
