package ir.reza.taskmanager.dto.request;

public record TaskRequestDto(
        String title,
        Integer estimatedMinutes,
        Long assigneeId,
        Long projectId
) {
}