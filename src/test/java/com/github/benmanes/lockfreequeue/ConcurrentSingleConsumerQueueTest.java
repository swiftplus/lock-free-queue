/* Copyright 2012 Ben Manes. All Rights Reserved. */
package com.github.benmanes.lockfreequeue;

import java.util.Iterator;
import java.util.Queue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

/**
 * A unit-test for {@link java.util.Queue} interface. These tests do not assert
 * correct concurrency behavior.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
public final class ConcurrentSingleConsumerQueueTest {
  private static final int WARMED_ARRAY_SIZE = 16;
  private static final int WARMED_LIST_SIZE = 32;

  @Test(dataProvider = "allQueues")
  public void clear(Queue<?> queue) {
    queue.clear();
    assertThat(queue, is(empty()));
  }

  @Test(dataProvider = "allQueues")
  public void estimatedCapacity(ConcurrentSingleConsumerQueue<?> queue) {
    assertThat(queue.estimatedCapacity(), is(WARMED_ARRAY_SIZE));
  }

  @Test
  public void estimatedCapacity_powerOfTwo() {
    ConcurrentSingleConsumerQueue<?> queue = new ConcurrentSingleConsumerQueue<Integer>(12);
    assertThat(queue.estimatedCapacity(), is(16));
  }

  @Test(dataProvider = "emptyQueue")
  public void size_whenEmpty(Queue<?> queue) {
    assertThat(queue.size(), is(0));
  }

  @Test(dataProvider = "warmedArrayQueue")
  public void size_whenArrayPopulated(Queue<?> queue) {
    assertThat(queue.size(), is(WARMED_ARRAY_SIZE));
  }

  @Test(dataProvider = "warmedListQueue")
  public void size_whenListPopulated(Queue<?> queue) {
    assertThat(queue.size(), is(WARMED_LIST_SIZE));
  }

  @Test(dataProvider = "emptyQueue")
  public void isEmpty_whenEmpty(Queue<?> queue) {
    assertThat(queue.isEmpty(), is(true));
  }

  @Test(dataProvider = "allWarmedQueues")
  public void isEmpty_whenPopulated(Queue<?> queue) {
    assertThat(queue.isEmpty(), is(false));
  }

  @Test(dataProvider = "emptyQueue")
  public void contains_withNull(Queue<?> queue) {
    assertThat(queue.contains(null), is(false));
  }

  @Test(dataProvider = "allWarmedQueues")
  public void contains_whenFound(Queue<?> queue) {
    assertThat(queue.contains(1), is(true));
  }

  @Test(dataProvider = "allWarmedQueues")
  public void contains_whenNotFound(Queue<?> queue) {
    assertThat(queue.contains(-1), is(false));
  }

  @Test(dataProvider = "emptyQueue", expectedExceptions = NullPointerException.class)
  public void offer_withNull(Queue<?> queue) {
    queue.offer(null);
  }

  @Test(dataProvider = "emptyQueue")
  public void offer_intoArray(ConcurrentSingleConsumerQueue<Integer> queue) {
    for (int i = 0; i < queue.estimatedCapacity(); i++) {
      queue.offer(i);
      assertThat(queue, hasItem(i));
    }
  }

  @Test(dataProvider = "emptyQueue")
  public void offer_intoLinkedList(ConcurrentSingleConsumerQueue<Integer> queue) {
    for (int i = 0; i < (2 * queue.estimatedCapacity()); i++) {
      queue.offer(i);
      assertThat(queue, hasItem(i));
    }
  }

  @Test(dataProvider = "emptyQueue")
  public void peek_whenEmpty(Queue<?> queue) {
    assertThat(queue.peek(), is(nullValue()));
  }

  @Test(dataProvider = "allWarmedQueues")
  public void peek(Queue<Integer> queue) {
    assertThat(queue.peek(), is(1));
  }

  @Test(dataProvider = "emptyQueue")
  public void poll_whenEmpty(Queue<?> queue) {
    assertThat(queue.poll(), is(nullValue()));
  }

  @Test(dataProvider = "allWarmedQueues")
  public void poll(Queue<Integer> queue) {
    int originalSize = queue.size();
    for (int i = 1; i <= originalSize; i++) {
      assertThat(queue.poll(), is(i));
      assertThat(queue.size(), is(originalSize - i));
    }
    assertThat(queue.size(), is(0));
  }

  @Test(dataProvider = "emptyQueue")
  public void drainTo_whenEmpty(ConcurrentSingleConsumerQueue<Integer> queue) {
    Integer[] out = new Integer[WARMED_ARRAY_SIZE];
    assertThat(queue.drainTo(out), is(0));
    assertThat(out, is(equalTo(new Integer[WARMED_ARRAY_SIZE])));
  }

  @Test(dataProvider = "allQueues")
  public void drainTo_withZeroSizeArray(ConcurrentSingleConsumerQueue<Integer> queue) {
    Integer[] out = new Integer[0];
    assertThat(queue.drainTo(out), is(0));
    assertThat(out, is(equalTo(new Integer[0])));
  }

  /* ---------------- Queue providers -------------- */

  @DataProvider(name = "allQueues")
  public Iterator<Object[]> providesAllQueues() {
    return Iterators.concat(providesEmptyQueue(), providesAllWarmedQueues());
  }

  @DataProvider(name = "emptyQueue")
  public Iterator<Object[]> providesEmptyQueue() {
    return ImmutableList.of(new Object[] { emptyQueue() }).iterator();
  }

  @DataProvider(name = "warmedArrayQueue")
  public Iterator<Object[]> providesWarmedArrayQueue() {
    return ImmutableList.of(
      new Object[] { warmedQueue(WARMED_ARRAY_SIZE) }).iterator();
  }

  @DataProvider(name = "warmedListQueue")
  public Iterator<Object[]> providesWarmedListQueue() {
    return ImmutableList.of(
      new Object[] { warmedQueue(WARMED_LIST_SIZE) }).iterator();
  }

  @DataProvider(name = "allWarmedQueues")
  public Iterator<Object[]> providesAllWarmedQueues() {
    return Iterators.concat(providesWarmedArrayQueue(), providesWarmedListQueue());
  }

  private Queue<Integer> emptyQueue() {
    return new ConcurrentSingleConsumerQueue<Integer>(WARMED_ARRAY_SIZE);
  }

  private Queue<Integer> warmedQueue(int count) {
    Queue<Integer> queue = new ConcurrentSingleConsumerQueue<Integer>(WARMED_ARRAY_SIZE);
    warmUp(queue, count);
    return queue;
  }

  private void warmUp(Queue<Integer> queue, int count) {
    for (int i = 0; i < count; i++) {
      queue.add(i + 1);
    }
  }
}
