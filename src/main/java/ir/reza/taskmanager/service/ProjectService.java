package ir.reza.taskmanager.service;

import ir.reza.taskmanager.command.ProjectRequestDtoToProject;
import ir.reza.taskmanager.command.ProjectToProjectResponseDto;
import ir.reza.taskmanager.dto.request.ProjectRequestDto;
import ir.reza.taskmanager.dto.response.ProjectResponseDto;
import ir.reza.taskmanager.entity.Project;
import ir.reza.taskmanager.exception.ResourceNotFoundException;
import ir.reza.taskmanager.repository.project.ProjectRepository;

import java.util.List;

public class ProjectService {

    private final ProjectRepository projectRepository;

    private final ProjectRequestDtoToProject requestConverter =
            new ProjectRequestDtoToProject();

    private final ProjectToProjectResponseDto responseConverter =
            new ProjectToProjectResponseDto();

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public List<ProjectResponseDto> findAll() {
        return projectRepository.findAll()
                .stream()
                .map(responseConverter::execute)
                .toList();
    }

    public ProjectResponseDto findById(Long id) {
        Project project = projectRepository.findById(id);

        if (project == null) {
            throw new ResourceNotFoundException(
                    "Project not found with id: " + id
            );
        }

        return responseConverter.execute(project);
    }

    public ProjectResponseDto add(ProjectRequestDto dto) {
        Project project = requestConverter.execute(dto);
        Project savedProject = projectRepository.add(project);

        return responseConverter.execute(savedProject);
    }
}