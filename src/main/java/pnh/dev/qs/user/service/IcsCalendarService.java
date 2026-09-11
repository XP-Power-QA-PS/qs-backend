package pnh.dev.qs.user.service;

import pnh.dev.qs.user.dto.MeetingEmailRequest;

public interface IcsCalendarService {
    byte[] generateMeetingIcs(MeetingEmailRequest request, String organizerEmail, String organizerName);
}
