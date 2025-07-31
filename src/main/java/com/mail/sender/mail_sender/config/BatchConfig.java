package com.mail.sender.mail_sender.config;



import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import com.mail.sender.mail_sender.dto.RecipientDTO;
import com.mail.sender.mail_sender.models.Recipient;
import com.mail.sender.mail_sender.repositories.RecipientRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfig {

    // @Autowired
    // private JobRepository jobRepository;

    // @Autowired
    // private PlatformTransactionManager platformTransactionManager;

    // @Autowired
    // private DataSource dataSource;

    @Bean
    @StepScope
    public FlatFileItemReader<RecipientDTO> reader(@Value("#{jobParameters['filePath']}") String filePath) {
        return new FlatFileItemReaderBuilder<RecipientDTO>()
                .name("recipientItemReader")
                .linesToSkip(1) // Don't want to process first row
                // .resource(new InputStreamResource(new ByteArrayInputStream(new byte[0])))
                .resource(new FileSystemResource(filePath))
                .delimited()
                .names("email", "firstName", "lastName", "phoneNumber", "company", "position")
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {
                    {
                        setTargetType(RecipientDTO.class);
                    }
                })
                // .targetType(RecipientDTO.class)
                .build();
    }

    // @Bean
    // public ItemProcessor<RecipientDTO, Recipient> processor(){
    // return new RecipientItemProcessor();
    // }

    @Bean
    @StepScope
    public RecipientItemProcessor processor(@Value("#{jobParameters['listId']}") Long listId,
    @Value("#{jobParameters['userId']}") Long userId) {
        RecipientItemProcessor processor = new RecipientItemProcessor();
        processor.setListId(listId);
        processor.setUserId(userId);
        return processor;
    }

    @Bean
    public RepositoryItemWriter<Recipient> writer(RecipientRepository recipientRepository) {
        return new RepositoryItemWriterBuilder<Recipient>()
                .repository(recipientRepository)
                .methodName("save")
                .build();
    }

    @Bean
    public Step importRecipientStep(final JobRepository repository, final PlatformTransactionManager transactionManager,
            RecipientRepository recipientRepository) {
        return new StepBuilder("importRecipientStep", repository)
                .<RecipientDTO, Recipient>chunk(100, transactionManager)
                .reader(reader(null))
                .processor(processor(null, null))
                .writer(writer(recipientRepository))
                .build();
    }

    @Bean
    public Job importRecipientJob(final JobRepository repository, PlatformTransactionManager transactionManager,
            RecipientRepository recipientRepository) {
        return new JobBuilder("importRecipientJob", repository)
                .incrementer(new RunIdIncrementer())
                .start(importRecipientStep(repository, transactionManager, recipientRepository))
                .build();
    }

}
