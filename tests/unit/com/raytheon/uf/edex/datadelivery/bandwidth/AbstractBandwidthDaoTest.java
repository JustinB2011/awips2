/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package com.raytheon.uf.edex.datadelivery.bandwidth;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.datadelivery.registry.Network;
import com.raytheon.uf.common.datadelivery.registry.OpenDapGriddedDataSetMetaData;
import com.raytheon.uf.common.datadelivery.registry.OpenDapGriddedDataSetMetaDataFixture;
import com.raytheon.uf.common.datadelivery.registry.SiteSubscriptionFixture;
import com.raytheon.uf.common.datadelivery.registry.Subscription;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.time.util.ImmutableDate;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.common.util.TestUtil;
import com.raytheon.uf.edex.datadelivery.bandwidth.dao.BandwidthAllocation;
import com.raytheon.uf.edex.datadelivery.bandwidth.dao.BandwidthAllocationFixture;
import com.raytheon.uf.edex.datadelivery.bandwidth.dao.BandwidthDataSetUpdate;
import com.raytheon.uf.edex.datadelivery.bandwidth.dao.BandwidthSubscription;
import com.raytheon.uf.edex.datadelivery.bandwidth.dao.IBandwidthDao;
import com.raytheon.uf.edex.datadelivery.bandwidth.dao.SubscriptionRetrieval;
import com.raytheon.uf.edex.datadelivery.bandwidth.dao.SubscriptionRetrievalFixture;
import com.raytheon.uf.edex.datadelivery.bandwidth.retrieval.RetrievalStatus;
import com.raytheon.uf.edex.datadelivery.bandwidth.util.BandwidthUtil;

/**
 * Test {@link IBandwidthDao} implementations.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 12, 2012 1286       djohnson     Initial creation
 * Jun 03, 2013 2038       djohnson     Add test getting retrievals by dataset, provider, and status.
 * 
 * </pre>
 * 
 * @author djohnson
 * @version 1.0
 */
@Ignore
public abstract class AbstractBandwidthDaoTest<T extends IBandwidthDao> {

    private T dao;

    @Before
    public void setUp() {
        this.dao = getDao();
    }

    /**
     * 
     * Return the {@link IBandwidthDao} implementation.
     * 
     * @return the dao
     */
    protected abstract T getDao();

    @Test
    public void testGetBandwidthAllocationForNetworkReturnsThoseWithSameNetwork() {
        SubscriptionRetrieval alloc1 = SubscriptionRetrievalFixture.INSTANCE
                .get();
        alloc1.setNetwork(Network.OPSNET);

        dao.store(alloc1.getBandwidthSubscription());
        dao.store(alloc1);

        assertEquals(1, dao.getBandwidthAllocations(Network.OPSNET).size());
    }

    @Test
    public void testGetBandwidthAllocationForNetworkDoesNotReturnThoseWithDifferentNetwork() {
        SubscriptionRetrieval alloc1 = SubscriptionRetrievalFixture.INSTANCE
                .get();
        alloc1.setNetwork(Network.SBN);

        dao.store(alloc1.getBandwidthSubscription());
        dao.store(alloc1);

        assertTrue(
                "Should not have returned an allocation not for the same network!",
                dao.getBandwidthAllocations(Network.OPSNET).isEmpty());
    }

    @Test
    public void testGetBandwidthAllocationForNetworkReturnsClones() {
        SubscriptionRetrieval alloc1 = SubscriptionRetrievalFixture.INSTANCE
                .get();
        alloc1.setNetwork(Network.OPSNET);

        dao.store(alloc1.getBandwidthSubscription());
        dao.store(alloc1);

        assertNotSame("Should have returned clones of the originals", alloc1,
                dao.getBandwidthAllocations(Network.OPSNET).iterator().next());
    }

    @Test
    public void testGetBandwidthAllocationsForSubIdReturnsOnlyThoseWithId() {
        SubscriptionRetrieval ret1 = SubscriptionRetrievalFixture.INSTANCE
                .get(1);
        SubscriptionRetrieval ret2 = SubscriptionRetrievalFixture.INSTANCE
                .get(2);

        dao.store(ret1.getBandwidthSubscription());
        dao.store(ret2.getBandwidthSubscription());
        dao.store(ret1);
        dao.store(ret2);

        List<BandwidthAllocation> results = dao.getBandwidthAllocations(ret2
                .getBandwidthSubscription().getId());
        assertEquals("Should have only returned one object!", 1, results.size());
        final BandwidthAllocation result = results.iterator().next();
        assertNotSame(ret2, result);
        assertEquals(ret2.getId(), result.getId());
    }

    @Test
    public void testGetBandwidthAllocationsInStateReturnsThoseInState() {
        SubscriptionRetrieval ret1 = SubscriptionRetrievalFixture.INSTANCE
                .get(1);
        ret1.setStatus(RetrievalStatus.CANCELLED);
        SubscriptionRetrieval ret2 = SubscriptionRetrievalFixture.INSTANCE
                .get(2);
        ret2.setStatus(RetrievalStatus.PROCESSING);

        dao.store(ret1.getBandwidthSubscription());
        dao.store(ret2.getBandwidthSubscription());
        dao.store(ret1);
        dao.store(ret2);

        final List<BandwidthAllocation> results = dao
                .getBandwidthAllocationsInState(RetrievalStatus.PROCESSING);
        assertEquals(1, results.size());
        final BandwidthAllocation result = results.iterator().next();
        assertEquals(RetrievalStatus.PROCESSING, result.getStatus());
        assertNotSame(ret2, result);
    }

    @Test
    public void testGetDataSetMetaDataDaoReturnsThoseWithSameDataSet() {
        OpenDapGriddedDataSetMetaData metaData = OpenDapGriddedDataSetMetaDataFixture.INSTANCE
                .get(1);
        OpenDapGriddedDataSetMetaData metaData2 = OpenDapGriddedDataSetMetaDataFixture.INSTANCE
                .get(2);
        BandwidthDataSetUpdate metaDataDao = dao
                .newBandwidthDataSetUpdate(metaData);
        dao.newBandwidthDataSetUpdate(metaData2);

        final List<BandwidthDataSetUpdate> results = dao
                .getBandwidthDataSetUpdate(metaData.getProviderName(),
                        metaData.getDataSetName());
        assertEquals(1, results.size());
        final BandwidthDataSetUpdate result = results.iterator().next();
        assertEquals(metaData.getDataSetName(), result.getDataSetName());
        assertNotSame(metaDataDao, result);
    }

    @Test
    public void testGetDataSetMetaDataDaoReturnsThoseWithSameDataSetAndBaseTime() {
        OpenDapGriddedDataSetMetaData metaData = OpenDapGriddedDataSetMetaDataFixture.INSTANCE
                .get(1);
        OpenDapGriddedDataSetMetaData metaData2 = OpenDapGriddedDataSetMetaDataFixture.INSTANCE
                .get(1);
        metaData2.setDate(new ImmutableDate(metaData.getDate().getTime()
                + TimeUtil.MILLIS_PER_YEAR));
        BandwidthDataSetUpdate metaDataDao = dao
                .newBandwidthDataSetUpdate(metaData);
        dao.newBandwidthDataSetUpdate(metaData2);

        final ImmutableDate date1 = metaData.getDate();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date1);

        final List<BandwidthDataSetUpdate> results = dao
                .getBandwidthDataSetUpdate(metaData.getProviderName(),
                        metaData.getDataSetName(), cal);
        assertEquals(1, results.size());
        final BandwidthDataSetUpdate result = results.iterator().next();
        assertEquals(metaData.getDataSetName(), result.getDataSetName());
        assertNotSame(metaDataDao, result);
    }

    @Test
    public void testGetDeferredForNetworkAndEndTimeReturnsThoseUpToEndTime() {
        Calendar now = BandwidthUtil.now();

        SubscriptionRetrieval alloc1 = SubscriptionRetrievalFixture.INSTANCE
                .get();
        alloc1.setNetwork(Network.OPSNET);
        alloc1.setStatus(RetrievalStatus.DEFERRED);
        alloc1.setEndTime(now);

        Calendar after = BandwidthUtil.copy(now);
        after.add(Calendar.HOUR, 1);

        SubscriptionRetrieval alloc2 = SubscriptionRetrievalFixture.INSTANCE
                .get();
        alloc2.setNetwork(Network.OPSNET);
        alloc2.setStatus(RetrievalStatus.DEFERRED);
        alloc2.setEndTime(after);

        dao.store(alloc1.getBandwidthSubscription());
        dao.store(alloc2.getBandwidthSubscription());
        dao.store(alloc1);
        dao.store(alloc2);

        final List<BandwidthAllocation> results = dao.getDeferred(
                Network.OPSNET, now);
        assertEquals(1, results.size());
        final BandwidthAllocation result = results.iterator().next();
        TestUtil.assertCalEquals("Expected the end time that equaled now!",
                now, result.getEndTime());
        assertNotSame(alloc1, result);
    }

    @Test
    public void testGetDeferredForNetworkAndEndTimeReturnsDeferred() {
        Calendar now = BandwidthUtil.now();

        SubscriptionRetrieval alloc1 = SubscriptionRetrievalFixture.INSTANCE
                .get();
        alloc1.setNetwork(Network.OPSNET);
        alloc1.setStatus(RetrievalStatus.DEFERRED);
        alloc1.setEndTime(now);

        SubscriptionRetrieval alloc2 = SubscriptionRetrievalFixture.INSTANCE
                .get();
        alloc2.setNetwork(Network.OPSNET);
        alloc2.setStatus(RetrievalStatus.FULFILLED);
        alloc2.setEndTime(now);

        dao.store(alloc1.getBandwidthSubscription());
        dao.store(alloc2.getBandwidthSubscription());
        dao.store(alloc1);
        dao.store(alloc2);

        final List<BandwidthAllocation> results = dao.getDeferred(
                Network.OPSNET, now);
        assertEquals(1, results.size());
        final BandwidthAllocation result = results.iterator().next();
        assertEquals("Expected DEFERRED status!", RetrievalStatus.DEFERRED,
                result.getStatus());
        assertNotSame(alloc1, result);
    }

    @Test
    public void testGetDeferredForNetworkAndEndTimeReturnsThoseWithNetwork() {
        Calendar now = BandwidthUtil.now();

        SubscriptionRetrieval alloc1 = SubscriptionRetrievalFixture.INSTANCE
                .get();
        alloc1.setNetwork(Network.OPSNET);
        alloc1.setStatus(RetrievalStatus.DEFERRED);
        alloc1.setEndTime(now);

        SubscriptionRetrieval alloc2 = SubscriptionRetrievalFixture.INSTANCE
                .get();
        alloc2.setNetwork(Network.SBN);
        alloc2.setStatus(RetrievalStatus.DEFERRED);
        alloc2.setEndTime(now);

        dao.store(alloc1.getBandwidthSubscription());
        dao.store(alloc2.getBandwidthSubscription());
        dao.store(alloc1);
        dao.store(alloc2);

        final List<BandwidthAllocation> results = dao.getDeferred(
                Network.OPSNET, now);
        assertEquals(1, results.size());
        final BandwidthAllocation result = results.iterator().next();
        assertEquals("Expected DEFERRED status!", RetrievalStatus.DEFERRED,
                result.getStatus());
        assertNotSame(alloc1, result);
    }

    @Test
    public void testGetSubscriptionDaoReturnsById()
            throws SerializationException {
        final Calendar now = BandwidthUtil.now();
        // Identical except for their identifier fields
        BandwidthSubscription entity1 = dao.newBandwidthSubscription(
                SiteSubscriptionFixture.INSTANCE.get(), now);
        BandwidthSubscription entity2 = dao.newBandwidthSubscription(
                SiteSubscriptionFixture.INSTANCE.get(), now);

        assertFalse("The two objects should not have the same id!",
                entity1.getId() == entity2.getId());

        final BandwidthSubscription result = dao
                .getBandwidthSubscription(entity2.getId());
        assertEquals("Should have returned the entity with the correct id!",
                entity2.getId(), result.getId());
        assertNotSame(entity2, result);
    }

    @Test
    public void testGetSubscriptionDaoByRegistryIdAndBaseTime()
            throws SerializationException {
        final Calendar now = BandwidthUtil.now();
        // Identical except for their base reference times and ids
        dao.newBandwidthSubscription(SiteSubscriptionFixture.INSTANCE.get(),
                now);

        final Calendar later = BandwidthUtil.now();
        later.add(Calendar.HOUR, 1);
        BandwidthSubscription entity2 = dao.newBandwidthSubscription(
                SiteSubscriptionFixture.INSTANCE.get(), later);

        final BandwidthSubscription result = dao.getBandwidthSubscription(
                entity2.getRegistryId(), later);
        assertEquals(
                "Should have returned the entity with the correct registryId!",
                entity2.getRegistryId(), result.getRegistryId());
        assertNotSame(entity2, result);
    }

    @Test
    public void testGetSubscriptionRetrievalById() {
        // Identical except for id
        SubscriptionRetrieval entity1 = SubscriptionRetrievalFixture.INSTANCE
                .get(1);
        SubscriptionRetrieval entity2 = SubscriptionRetrievalFixture.INSTANCE
                .get(1);

        dao.store(entity1.getBandwidthSubscription());
        dao.store(entity2.getBandwidthSubscription());
        dao.store(Arrays.asList(entity1, entity2));

        final SubscriptionRetrieval result = dao
                .getSubscriptionRetrieval(entity2.getId());
        assertEquals("Should have returned the entity with the correct id!",
                entity2.getId(), result.getId());
        assertNotSame(entity2, result);
    }

    @Test
    public void testGetSubscriptionRetrievalsByProviderAndDataSet()
            throws SerializationException {
        // These two have the same dataset name and provider
        SubscriptionRetrieval entity1 = SubscriptionRetrievalFixture.INSTANCE
                .get(1);
        SubscriptionRetrieval entity2 = SubscriptionRetrievalFixture.INSTANCE
                .get(1);
        // This one does not
        SubscriptionRetrieval entity3 = SubscriptionRetrievalFixture.INSTANCE
                .get(2);

        dao.store(entity1.getBandwidthSubscription());
        dao.store(entity2.getBandwidthSubscription());
        dao.store(entity3.getBandwidthSubscription());
        dao.store(Arrays.asList(entity1, entity2, entity3));

        final Subscription subscription = entity1.getSubscription();
        final String expectedProvider = subscription.getProvider();
        final String expectedDataSetName = subscription.getDataSetName();
        final List<SubscriptionRetrieval> results = dao
                .getSubscriptionRetrievals(expectedProvider,
                        expectedDataSetName);
        assertEquals(
                "Should have returned the two entities for the same dataset!",
                2, results.size());

        for (SubscriptionRetrieval retrieval : results) {
            assertEquals("Incorrect provider found.",
                    subscription.getProvider(), retrieval.getSubscription()
                            .getProvider());
            assertEquals("Incorrect data set found.",
                    subscription.getDataSetName(), retrieval.getSubscription()
                            .getDataSetName());
        }
    }

    @Test
    public void testGetSubscriptionRetrievalsByProviderDataSetAndBaseReferenceTime()
            throws SerializationException {
        // These two have the same dataset name and provider
        SubscriptionRetrieval entity1 = SubscriptionRetrievalFixture.INSTANCE
                .get(1);
        SubscriptionRetrieval entity2 = SubscriptionRetrievalFixture.INSTANCE
                .get(1);
        // This one does not
        SubscriptionRetrieval entity3 = SubscriptionRetrievalFixture.INSTANCE
                .get(2);

        // Still have to persist the actual subscription daos
        final BandwidthSubscription subDao1 = entity1
                .getBandwidthSubscription();
        final BandwidthSubscription subDao2 = entity2
                .getBandwidthSubscription();
        final BandwidthSubscription subDao3 = entity3
                .getBandwidthSubscription();

        // Give each a unique time
        final Calendar one = BandwidthUtil.now();
        subDao1.setBaseReferenceTime(one);
        final Calendar two = BandwidthUtil.copy(one);
        two.add(Calendar.HOUR, 1);
        subDao2.setBaseReferenceTime(two);
        final Calendar three = BandwidthUtil.copy(two);
        three.add(Calendar.HOUR, 1);
        subDao3.setBaseReferenceTime(three);

        // This persists the subscription dao objects and sets them on the
        // retrievals
        entity1.setBandwidthSubscription(dao.newBandwidthSubscription(
                subDao1.getSubscription(), subDao1.getBaseReferenceTime()));
        entity2.setBandwidthSubscription(dao.newBandwidthSubscription(
                subDao2.getSubscription(), subDao2.getBaseReferenceTime()));
        entity3.setBandwidthSubscription(dao.newBandwidthSubscription(
                subDao3.getSubscription(), subDao3.getBaseReferenceTime()));

        dao.store(Arrays.asList(entity1, entity2, entity3));

        final Subscription subscription = entity1.getSubscription();
        final String expectedProvider = subscription.getProvider();
        final String expectedDataSetName = subscription.getDataSetName();
        final List<SubscriptionRetrieval> results = dao
                .getSubscriptionRetrievals(expectedProvider,
                        expectedDataSetName, one);
        assertEquals(
                "Should have returned one entity for the provider, dataset, and base reference time!",
                1, results.size());

        SubscriptionRetrieval result = results.iterator().next();
        final Subscription resultSubscription = result.getSubscription();
        assertEquals("Incorrect provider found.", subscription.getProvider(),
                resultSubscription.getProvider());
        assertEquals("Incorrect data set found.",
                subscription.getDataSetName(),
                resultSubscription.getDataSetName());
        TestUtil.assertCalEquals("Wrong base reference time found.", one,
                result.getBandwidthSubscription().getBaseReferenceTime());
    }

    @Test
    public void testGetSubscriptionsReturnsClones()
            throws SerializationException {
        BandwidthSubscription entity = dao.newBandwidthSubscription(
                SiteSubscriptionFixture.INSTANCE.get(), BandwidthUtil.now());

        List<BandwidthSubscription> results = dao.getBandwidthSubscriptions();
        assertEquals(1, results.size());
        BandwidthSubscription result = results.iterator().next();
        assertNotSame(entity, result);
    }

    @Test
    public void testGetSubscriptionsByProviderDataSetAndBaseReferenceTime()
            throws SerializationException {
        final Calendar one = BandwidthUtil.now();
        final Calendar two = BandwidthUtil.copy(one);
        two.add(Calendar.HOUR, 1);
        final Calendar three = BandwidthUtil.copy(two);
        three.add(Calendar.HOUR, 1);

        // Three entities all the same except for base reference time
        dao.newBandwidthSubscription(SiteSubscriptionFixture.INSTANCE.get(),
                one);
        dao.newBandwidthSubscription(SiteSubscriptionFixture.INSTANCE.get(),
                two);
        BandwidthSubscription entity3 = dao.newBandwidthSubscription(
                SiteSubscriptionFixture.INSTANCE.get(), three);
        // One with same base reference time but different provider/dataset
        dao.newBandwidthSubscription(SiteSubscriptionFixture.INSTANCE.get(2),
                three);

        List<BandwidthSubscription> results = dao.getBandwidthSubscriptions(
                entity3.getProvider(), entity3.getDataSetName(), three);
        assertEquals(1, results.size());
        BandwidthSubscription result = results.iterator().next();
        assertEquals("Incorrect provider", entity3.getProvider(),
                result.getProvider());
        assertEquals("Incorrect provider", entity3.getDataSetName(),
                result.getDataSetName());
        TestUtil.assertCalEquals("Incorrect base reference time.", three,
                result.getBaseReferenceTime());
        assertNotSame(entity3, result);
    }

    @Test
    public void testQuerySubscriptionRetrievalsBySubscriptionId()
            throws SerializationException {
        // These two have the same dataset name and provider
        SubscriptionRetrieval entity1 = SubscriptionRetrievalFixture.INSTANCE
                .get(1);
        SubscriptionRetrieval entity2 = SubscriptionRetrievalFixture.INSTANCE
                .get(1);
        // This one does not
        SubscriptionRetrieval entity3 = SubscriptionRetrievalFixture.INSTANCE
                .get(2);

        // Still have to persist the actual subscription daos
        final BandwidthSubscription subDao1 = entity1
                .getBandwidthSubscription();
        final BandwidthSubscription subDao2 = entity2
                .getBandwidthSubscription();
        final BandwidthSubscription subDao3 = entity3
                .getBandwidthSubscription();

        // This persists the subscription dao objects and sets them on the
        // retrievals
        entity1.setBandwidthSubscription(dao.newBandwidthSubscription(
                subDao1.getSubscription(), subDao1.getBaseReferenceTime()));
        entity2.setBandwidthSubscription(dao.newBandwidthSubscription(
                subDao2.getSubscription(), subDao2.getBaseReferenceTime()));
        entity3.setBandwidthSubscription(dao.newBandwidthSubscription(
                subDao3.getSubscription(), subDao3.getBaseReferenceTime()));

        dao.store(Arrays.asList(entity1, entity2, entity3));
        final List<SubscriptionRetrieval> results = dao
                .querySubscriptionRetrievals(entity2.getBandwidthSubscription()
                        .getId());
        assertEquals(
                "Should have returned one entity for the subscriptionDao id!",
                1, results.size());

        SubscriptionRetrieval result = results.iterator().next();
        assertEquals("Incorrect id found.", entity2.getId(), result.getId());
        assertNotSame(entity2, result);
    }

    @Test
    public void testQuerySubscriptionRetrievalsBySubscription()
            throws SerializationException {
        // These two have the same dataset name and provider
        SubscriptionRetrieval entity1 = SubscriptionRetrievalFixture.INSTANCE
                .get(1);
        SubscriptionRetrieval entity2 = SubscriptionRetrievalFixture.INSTANCE
                .get(1);
        // This one does not
        SubscriptionRetrieval entity3 = SubscriptionRetrievalFixture.INSTANCE
                .get(2);

        // Still have to persist the actual subscription daos
        final BandwidthSubscription subDao1 = entity1
                .getBandwidthSubscription();
        final BandwidthSubscription subDao2 = entity2
                .getBandwidthSubscription();
        final BandwidthSubscription subDao3 = entity3
                .getBandwidthSubscription();

        // This persists the subscription dao objects and sets them on the
        // retrievals
        entity1.setBandwidthSubscription(dao.newBandwidthSubscription(
                subDao1.getSubscription(), subDao1.getBaseReferenceTime()));
        entity2.setBandwidthSubscription(dao.newBandwidthSubscription(
                subDao2.getSubscription(), subDao2.getBaseReferenceTime()));
        entity3.setBandwidthSubscription(dao.newBandwidthSubscription(
                subDao3.getSubscription(), subDao3.getBaseReferenceTime()));

        dao.store(Arrays.asList(entity1, entity2, entity3));
        final List<SubscriptionRetrieval> results = dao
                .querySubscriptionRetrievals(entity2.getBandwidthSubscription());
        assertEquals(
                "Should have returned one entity for the subscriptionDao!", 1,
                results.size());

        SubscriptionRetrieval result = results.iterator().next();
        assertEquals("Incorrect id found.", entity2.getId(), result.getId());
        assertNotSame(entity2, result);
    }

    @Test
    public void testRemoveSubscriptionDao() throws SerializationException {
        final Calendar now = BandwidthUtil.now();
        dao.newBandwidthSubscription(SiteSubscriptionFixture.INSTANCE.get(1),
                now);
        final BandwidthSubscription entity2 = dao.newBandwidthSubscription(
                SiteSubscriptionFixture.INSTANCE.get(2), now);
        dao.newBandwidthSubscription(SiteSubscriptionFixture.INSTANCE.get(3),
                now);

        assertEquals("Incorrect number of entities found!", 3, dao
                .getBandwidthSubscriptions().size());

        dao.remove(entity2);

        final List<BandwidthSubscription> subscriptions = dao
                .getBandwidthSubscriptions();
        assertEquals("Incorrect number of entities found!", 2,
                subscriptions.size());
        for (BandwidthSubscription subscription : subscriptions) {
            assertFalse(
                    "Should not have found the entity with the removed entity's id",
                    subscription.getId() == entity2.getId());
        }

    }

    @Test
    public void testStoreSetsBandwidthAllocationId() {
        BandwidthAllocation entity = BandwidthAllocationFixture.INSTANCE.get();
        entity.setAgentType("someAgentType");

        assertEquals("The id should not have been set!",
                BandwidthUtil.DEFAULT_IDENTIFIER, entity.getId());
        dao.store(entity);
        assertFalse("The id should have been set!",
                BandwidthUtil.DEFAULT_IDENTIFIER == entity.getId());
    }

    @Test
    public void testStoreSetsSubscriptionRetrievalId() {
        SubscriptionRetrieval entity = SubscriptionRetrievalFixture.INSTANCE
                .get();
        assertEquals("The id should not have been set!",
                BandwidthUtil.DEFAULT_IDENTIFIER, entity.getId());
        dao.store(entity.getBandwidthSubscription());
        dao.store(Arrays.asList(entity));
        assertFalse("The id should have been set!",
                BandwidthUtil.DEFAULT_IDENTIFIER == entity.getId());
    }

    @Test
    public void testUpdateBandwidthAllocation() {
        final long estimatedSize = 25L;

        BandwidthAllocation entity = BandwidthAllocationFixture.INSTANCE.get();
        entity.setAgentType("someAgentType");
        dao.store(entity);
        entity.setEstimatedSize(estimatedSize);
        dao.createOrUpdate(entity);

        assertEquals("Expected the entity to have been updated!", 25L, dao
                .getBandwidthAllocations(entity.getNetwork()).iterator().next()
                .getEstimatedSize());
    }

    @Test
    public void testUpdateSubscriptionDao() throws SerializationException {
        final long estimatedSize = 25L;

        BandwidthSubscription entity = dao.newBandwidthSubscription(
                SiteSubscriptionFixture.INSTANCE.get(), BandwidthUtil.now());

        entity.setEstimatedSize(estimatedSize);
        dao.update(entity);

        assertEquals("Expected the entity to have been updated!", 25L, dao
                .getBandwidthSubscriptions().iterator().next()
                .getEstimatedSize());
    }

    @Test
    public void testUpdateSubscriptionRetrieval() {
        final long estimatedSize = 25L;

        SubscriptionRetrieval entity = SubscriptionRetrievalFixture.INSTANCE
                .get();
        dao.store(entity.getBandwidthSubscription());
        dao.store(entity);
        entity.setEstimatedSize(estimatedSize);
        dao.update(entity);

        assertEquals("Expected the entity to have been updated!", 25L, dao
                .getSubscriptionRetrieval(entity.getId()).getEstimatedSize());
    }

    @Test
    public void testGetSubscriptionRetrievalsByProviderDataSetAndStatus() {

        final int numberOfScheduledEntities = 2;
        final int numberOfReadyEntities = 3;
        List<SubscriptionRetrieval> entities = Lists
                .newArrayListWithCapacity(numberOfScheduledEntities
                        + numberOfReadyEntities);

        // Create some scheduled entities
        entities.addAll(getEntitiesInState(numberOfScheduledEntities,
                RetrievalStatus.SCHEDULED));

        // Create some ready entities
        entities.addAll(getEntitiesInState(numberOfReadyEntities,
                RetrievalStatus.READY));

        for (int i = 0; i < entities.size(); i++) {
            final SubscriptionRetrieval entity = entities.get(i);
            // Give each one a unique start time
            entity.getStartTime().add(Calendar.MINUTE, i);
            dao.store(entity.getBandwidthSubscription());
        }
        dao.store(entities);

        BandwidthSubscription bandwidthSubscription = entities.iterator()
                .next().getBandwidthSubscription();
        final int actualNumberOfScheduledStatus = dao
                .getSubscriptionRetrievals(bandwidthSubscription.getProvider(),
                        bandwidthSubscription.getDataSetName(),
                        RetrievalStatus.SCHEDULED).size();
        assertThat(actualNumberOfScheduledStatus,
                is(equalTo(numberOfScheduledEntities)));

        final int actualNumberOfReadyStatus = dao.getSubscriptionRetrievals(
                bandwidthSubscription.getProvider(),
                bandwidthSubscription.getDataSetName(), RetrievalStatus.READY)
                .size();
        assertThat(actualNumberOfReadyStatus,
                is(equalTo(numberOfReadyEntities)));
    }

    @Test
    public void testGetSubscriptionRetrievalsByProviderDataSetStatusAndDates() {

        final int numberOfScheduledEntities = 2;
        final int numberOfReadyEntities = 10;
        List<SubscriptionRetrieval> entities = Lists
                .newArrayListWithCapacity(numberOfScheduledEntities
                        + numberOfReadyEntities);

        // Create some scheduled entities
        entities.addAll(getEntitiesInState(numberOfScheduledEntities,
                RetrievalStatus.SCHEDULED));

        // Create some ready entities
        List<SubscriptionRetrieval> readyEntities = getEntitiesInState(
                numberOfReadyEntities, RetrievalStatus.READY);
        entities.addAll(readyEntities);

        // Persist the bandwidth subscriptions and create some unique times
        for (int i = 0; i < entities.size(); i++) {
            final SubscriptionRetrieval entity = entities.get(i);

            // Give each one a unique start time
            final Calendar startTime = entity.getStartTime();
            startTime.add(Calendar.HOUR, i);

            // ... and end time
            Calendar endTime = BandwidthUtil.copy(startTime);
            endTime.add(Calendar.MINUTE, 5);
            entity.setEndTime(endTime);

            dao.store(entity.getBandwidthSubscription());
        }
        dao.store(entities);

        BandwidthSubscription bandwidthSubscription = entities.iterator()
                .next().getBandwidthSubscription();
        // These are the entities we expect to get (two items)
        List<SubscriptionRetrieval> expectToGet = readyEntities.subList(3, 5);
        final Iterator<SubscriptionRetrieval> iter = expectToGet.iterator();

        // Use the start time of the first retrieval and the end time of the
        // second retrieval
        final Date startTime = iter.next().getStartTime().getTime();
        final Date endTime = iter.next().getEndTime().getTime();

        final SortedSet<SubscriptionRetrieval> actualReceived = dao.getSubscriptionRetrievals(bandwidthSubscription.getProvider(),
                bandwidthSubscription.getDataSetName(), RetrievalStatus.READY,
                startTime, endTime);

        // Verify the correct number of retrievals were returned
        assertThat(actualReceived, hasSize(expectToGet.size()));

        // Verify the two SubscriptionRetrievals are correct
        Iterator<SubscriptionRetrieval> actualIter = actualReceived.iterator();
        assertThat(actualIter.next(), is(equalTo(expectToGet.get(0))));
        assertThat(actualIter.next(), is(equalTo(expectToGet.get(1))));
    }

    /**
     * Get the specified number of entities in the specified state.
     * 
     * @param numberOfEntities
     * @param state
     * @return the entities
     */
    protected static List<SubscriptionRetrieval> getEntitiesInState(
            int numberOfEntities, RetrievalStatus state) {
        List<SubscriptionRetrieval> entities = Lists.newArrayList();
        for (int i = 0; i < numberOfEntities; i++) {
            SubscriptionRetrieval entity = SubscriptionRetrievalFixture.INSTANCE
                    .get();
            entity.setStatus(state);
            entities.add(entity);
        }
        return entities;
    }
}
