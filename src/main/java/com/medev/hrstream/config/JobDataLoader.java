package com.medev.hrstream.config;

import com.medev.hrstream.job.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Component
public class JobDataLoader implements CommandLineRunner {

    private final JobRepository jobRepository;

    @Value("${app.seed.jobs.enabled:true}")
    private boolean seedJobsEnabled;

    public JobDataLoader(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Only load data if seeding is enabled and the database is empty
        if (!seedJobsEnabled) {
            System.out.println("Job seeding is disabled via configuration. Skipping.");
            return;
        }
        
        if (jobRepository.count() == 0) {
            System.out.println("Loading 30 random jobs into the database...");
            
            Random random = new Random();
            
            // Job titles
            List<String> titles = Arrays.asList(
                "Senior Java Developer", "Frontend React Developer", "Backend Node.js Engineer",
                "Full Stack Developer", "DevOps Engineer", "Data Scientist",
                "Machine Learning Engineer", "Cloud Architect", "Cybersecurity Specialist",
                "UI/UX Designer", "Product Manager", "QA Automation Engineer",
                "Mobile App Developer", "Database Administrator", "System Analyst",
                "Technical Lead", "Software Architect", "Python Developer",
                "Go Developer", "Ruby on Rails Developer", "Scala Engineer",
                "Kotlin Developer", "TypeScript Developer", "PHP Developer",
                "Blockchain Developer", "AI Research Scientist", "Data Analyst",
                "IT Support Specialist", "Network Engineer", "Sales Engineer"
            );

            // Locations
            List<String> locations = Arrays.asList(
                "Paris, France", "London, UK", "Berlin, Germany", "Amsterdam, Netherlands",
                "Madrid, Spain", "Rome, Italy", "Brussels, Belgium", "Lyon, France",
                "Marseille, France", "Toulouse, France", "Bordeaux, France", "Lille, France",
                "Nantes, France", "Nice, France", "Strasbourg, France", "Rennes, France",
                "Remote", "Hybrid", "New York, USA", "San Francisco, USA"
            );

            // Experience levels
            List<String> experienceLevels = Arrays.asList(
                "Junior", "Mid-level", "Senior", "Lead", "Principal", "Intern"
            );

            // Company details
            List<String> companies = Arrays.asList(
                "TechCorp", "Innovate Solutions", "Digital Dynamics", "WebWorks",
                "Data Systems", "Cloud Nine", "Nexus Technologies", "Alpha Software",
                "Beta Innovations", "Gamma Labs", "Omega Systems", "Sigma Tech",
                "Epsilon Solutions", "Zeta Corporation", "Eta Enterprises", "Theta Industries",
                "Iota LLC", "Kappa Group", "Lambda Inc", "Mu Technologies"
            );

            // Required skills
            List<List<String>> requiredSkillsSets = Arrays.asList(
                Arrays.asList("Java", "Spring Boot", "Hibernate", "SQL"),
                Arrays.asList("JavaScript", "React", "Redux", "HTML", "CSS"),
                Arrays.asList("Node.js", "Express", "MongoDB", "REST API"),
                Arrays.asList("Python", "Django", "Flask", "PostgreSQL"),
                Arrays.asList("Docker", "Kubernetes", "AWS", "CI/CD"),
                Arrays.asList("Python", "TensorFlow", "PyTorch", "Machine Learning"),
                Arrays.asList("AWS", "Azure", "GCP", "Terraform"),
                Arrays.asList("Figma", "Sketch", "Adobe XD", "UI Design"),
                Arrays.asList("Agile", "Scrum", "Product Management", "JIRA"),
                Arrays.asList("Selenium", "TestNG", "Java", "Automation Testing"),
                Arrays.asList("Swift", "Kotlin", "Android", "iOS"),
                Arrays.asList("MySQL", "PostgreSQL", "Oracle", "Database Design"),
                Arrays.asList("UML", "Requirements Analysis", "System Design")
            );

            // Nice to have skills
            List<List<String>> niceToHaveSkillsSets = Arrays.asList(
                Arrays.asList("Kafka", "Redis", "Elasticsearch"),
                Arrays.asList("TypeScript", "Next.js", "GraphQL"),
                Arrays.asList("TypeORM", "NestJS", "WebSockets"),
                Arrays.asList("FastAPI", "Pandas", "NumPy"),
                Arrays.asList("Jenkins", "GitLab CI", "Prometheus"),
                Arrays.asList("Computer Vision", "NLP", "Data Visualization"),
                Arrays.asList("Ansible", "Puppet", "Chef"),
                Arrays.asList("InVision", "Prototype Design", "User Research"),
                Arrays.asList("SAFe", "Lean", "Kanban"),
                Arrays.asList("Cucumber", "Appium", "Postman"),
                Arrays.asList("React Native", "Flutter", "Xamarin"),
                Arrays.asList("NoSQL", "Redis", "MongoDB"),
                Arrays.asList("Microservices", "Domain-Driven Design")
            );

            // Additional info
            List<String> additionalInfos = Arrays.asList(
                "Fast-paced environment with growth opportunities",
                "Flexible working hours and remote options",
                "Competitive salary and benefits package",
                "International team and projects",
                "Cutting-edge technologies and innovation",
                "Training and development programs available",
                "Work-life balance encouraged",
                "Modern office with great amenities",
                "Stock options and performance bonuses",
                "Health insurance and retirement plans included",
                "Collaborative team culture",
                "Opportunities for international travel",
                "Mentorship programs for junior developers"
            );

            // Descriptions
            List<String> descriptions = Arrays.asList(
                "We are looking for a talented developer to join our team and help build innovative solutions.",
                "Exciting opportunity to work on cutting-edge projects with modern technologies.",
                "Join our dynamic team and contribute to the development of next-generation applications.",
                "We need a passionate individual who can design and implement robust software solutions.",
                "Great chance to work with a collaborative team on challenging and impactful projects.",
                "Looking for a skilled professional to help us deliver high-quality software products.",
                "Opportunity to work in an agile environment with continuous learning and growth.",
                "We offer a competitive package and the chance to work with the latest technologies.",
                "Join us and be part of a team that values innovation, quality, and teamwork.",
                "Excellent opportunity for career development and working on diverse projects."
            );

            for (int i = 0; i < 30; i++) {
                String title = titles.get(random.nextInt(titles.size()));
                String description = descriptions.get(random.nextInt(descriptions.size())) + 
                                    " Requirements: " + String.join(", ", requiredSkillsSets.get(random.nextInt(requiredSkillsSets.size()))) + 
                                    ". Experience level: " + experienceLevels.get(random.nextInt(experienceLevels.size())) + 
                                    ". Location: " + locations.get(random.nextInt(locations.size())) + ".";

                Job job = Job.builder()
                    .title(title)
                    .description(description)
                    .location(locations.get(random.nextInt(locations.size())))
                    .experienceLevel(experienceLevels.get(random.nextInt(experienceLevels.size())))
                    .contractType(ContractType.values()[random.nextInt(ContractType.values().length)])
                    .companyDetails(companies.get(random.nextInt(companies.size())) + " - " + 
                                   (random.nextBoolean() ? "Established company" : "Fast-growing startup"))
                    .additionalInfo(additionalInfos.get(random.nextInt(additionalInfos.size())))
                    .requiredSkills(requiredSkillsSets.get(random.nextInt(requiredSkillsSets.size())))
                    .niceToHaveSkills(niceToHaveSkillsSets.get(random.nextInt(niceToHaveSkillsSets.size())))
                    .applyLink("https://hrstream.example.com/apply/" + UUID.randomUUID().toString())
                    .applicationToken(UUID.randomUUID().toString())
                    .status(JobStatus.OPEN)
                    .dateLimte(LocalDateTime.now().plusMonths(random.nextInt(3) + 1))
                    .deleted(false)
                    .createdBy("system")
                    .build();

                jobRepository.save(job);
            }
            
            System.out.println("Successfully loaded " + jobRepository.count() + " jobs!");
        } else {
            System.out.println("Database already contains " + jobRepository.count() + " jobs. Skipping data load.");
        }
    }
}
