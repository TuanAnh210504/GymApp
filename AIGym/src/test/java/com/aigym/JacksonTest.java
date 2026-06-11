package com.aigym;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.aigym.dto.WeeklySchedule.WeeklyScheduleRequest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class JacksonTest {
    @Test
    public void testDeserialization() throws Exception {
        String json = "{\n" +
                "  \"name\": \"Giáo án Push Pull Legs\",\n" +
                "  \"description\": \"Test.\",\n" +
                "  \"active\": true,\n" +
                "  \"scheduleDays\": [\n" +
                "    {\n" +
                "      \"dayOfWeek\": \"TUESDAY\",\n" +
                "      \"label\": \"Ngày Nghỉ\",\n" +
                "      \"restDay\": true,\n" +
                "      \"scheduledExercises\": []\n" +
                "    },\n" +
                "    {\n" +
                "      \"dayOfWeek\": \"MONDAY\",\n" +
                "      \"label\": \"Ngày Tập\",\n" +
                "      \"restDay\": false,\n" +
                "      \"scheduledExercises\": []\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);
        try {
            WeeklyScheduleRequest req = mapper.readValue(json, WeeklyScheduleRequest.class);
            assertNotNull(req);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
