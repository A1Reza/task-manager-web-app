package ir.reza.taskmanager.dto.response;

public record TaskResponseDto(
        Long id,
        String title,
        Integer estimatedMinutes,
        Long assigneeId,
        String assigneeName,
        Long projectId,
        String projectName
) {
}