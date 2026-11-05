INSERT INTO student_projects (id, title, description, start_date, end_date) VALUES
    (1, 'ResuMatch Recommendation API', 'Built a Spring Boot recommendation service using Java, REST APIs, JPA, and ranking algorithms.', '2024-01-15', '2025-03-01'),
    (2, 'Campus Analytics Dashboard', 'Created a Python data pipeline and machine learning dashboard to analyze student engagement.', '2023-08-01', '2024-02-15'),
    (3, 'Cloud Deployment Toolkit', 'Automated Docker container builds and Kubernetes deployments for cloud-native applications.', '2025-01-10', NULL);

INSERT INTO project_skill_tags (project_id, skill_tag) VALUES
    (1, 'Java'),
    (1, 'Spring Boot'),
    (1, 'REST'),
    (1, 'JPA'),
    (1, 'Algorithms'),
    (2, 'Python'),
    (2, 'Machine Learning'),
    (2, 'Data Analytics'),
    (3, 'Docker'),
    (3, 'Kubernetes'),
    (3, 'CI/CD'),
    (3, 'Cloud');
