package uk.gov.justice.digital.hmpps.keyworker.repository;

import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.hmpps.keyworker.model.*;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)

@Transactional

public class OffenderKeyworkerRepositoryTest {

    private static final LocalDateTime ASSIGNED_DATE_TIME = LocalDateTime.of(2016,1, 2, 3, 4, 5);
    private static final LocalDateTime EXPIRY_DATE_TIME = LocalDateTime.of(2020, 12, 30, 9, 34, 56);
    private static long currentId;

    @Autowired
    private OffenderKeyworkerRepository repository;

    @Test
    public void givenATransientOffenderKeyworkerWhenPersistedIitShoudBeRetrievableById() {

        val transientEntity = transientEntity();

        val entity = transientEntity.toBuilder().build();

        val persistedEntity = repository.save(entity);

        TestTransaction.flagForCommit();
        TestTransaction.end();

        assertThat(persistedEntity.getOffenderKeyworkerId()).isNotNull();

        TestTransaction.start();

        val retrievedEntity = repository.findOne(entity.getOffenderKeyworkerId());

        // equals only compares the business key columns: offenderBookingId, staffId and assignedDateTime
        assertThat(retrievedEntity).isEqualTo(transientEntity);

        assertThat(retrievedEntity.isActive()).isTrue();
        assertThat(retrievedEntity.getAllocationType()).isEqualTo(AllocationType.AUTO);
        assertThat(retrievedEntity.getUserId()).isEqualTo("The Assigning User");
        assertThat(retrievedEntity.getAgencyId()).isEqualTo("LEI");
        assertThat(retrievedEntity.getExpiryDateTime()).isEqualTo(EXPIRY_DATE_TIME);
        assertThat(retrievedEntity.getDeallocationReason()).isEqualTo(DeallocationReason.OVERRIDE);
        assertThat(retrievedEntity.getCreateUpdate()).isEqualTo(transientEntity.getCreateUpdate());
    }

    @Test
    public void givenAPersistentInstanceThenNullableValuesAreUpdateable() {

        val entity = repository.save(transientEntity());
        TestTransaction.flagForCommit();
        TestTransaction.end();

        TestTransaction.start();
        val retrievedEntity = repository.findOne(entity.getOffenderKeyworkerId());

        assertThat(retrievedEntity.getCreateUpdate().getModifyDateTime()).isNull();
        assertThat(retrievedEntity.getCreateUpdate().getModifyUserId()).isNull();

        retrievedEntity.setCreateUpdate((addUpdateInfo(retrievedEntity.getCreateUpdate())));

        TestTransaction.flagForCommit();
        TestTransaction.end();

        TestTransaction.start();

        val persistedUpdates = repository.findOne(entity.getOffenderKeyworkerId());

        assertThat(persistedUpdates.getCreateUpdate().getModifyDateTime()).isNotNull();
        assertThat(persistedUpdates.getCreateUpdate().getModifyUserId()).isEqualTo("Modify User Id");
    }

    private OffenderKeyworker transientEntity() {
        return OffenderKeyworker
                .builder()
                .offenderNo("A1234AA")
                .staffId(nextId())
                .assignedDateTime(ASSIGNED_DATE_TIME)
                .active(true)
                .allocationReason(AllocationReason.MANUAL)
                .allocationType(AllocationType.AUTO)
                .userId("The Assigning User")
                .agencyId("LEI")
                .expiryDateTime(EXPIRY_DATE_TIME)
                .deallocationReason(DeallocationReason.OVERRIDE)
                .createUpdate(creationTimeInfo())
                .build();
    }

    private CreateUpdate creationTimeInfo() {
        return CreateUpdate
                .builder()
                .creationDateTime(LocalDateTime.now())
                .createUserId("Creation User Id")
                .build();
    }

    private CreateUpdate addUpdateInfo(CreateUpdate createUpdate) {
        return createUpdate
                .toBuilder()
                .modifyDateTime(LocalDateTime.now())
                .modifyUserId("Modify User Id")
                .build();
    }

    private static long nextId() {
        return currentId++;
    }
}