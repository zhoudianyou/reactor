/*
 * Copyright (c) 2011-2015 Pivotal Software Inc, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package reactor.core.processor;

import org.reactivestreams.Subscriber;
import org.reactivestreams.tck.SubscriberWhiteboxVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.annotations.Test;
import reactor.core.reactivestreams.PublisherFactory;
import reactor.core.reactivestreams.SubscriberFactory;

import java.util.Random;

/**
 * @author Stephane Maldini
 */
@Test
public class SubscriberFactoryTests extends SubscriberWhiteboxVerification<Long> {

	private final Random random = new Random();

	public SubscriberFactoryTests() {
		super(new TestEnvironment(2000, true));
	}

	@Override
	public Subscriber<Long> createSubscriber(final WhiteboxSubscriberProbe<Long> probe) {
		return SubscriberFactory.create(
				subscription -> {
					probe.registerOnSubscribe(new SubscriberWhiteboxVerification.SubscriberPuppet() {

						@Override
						public void triggerRequest(long elements) {
							subscription.request(elements);
						}

						@Override
						public void signalCancel() {
							subscription.cancel();
						}
					});
					//subscription.request(1);
					return probe;
				},
				(data, subscription) -> {
					subscription.context().registerOnNext(data);
					//subscription.request(1);
				},
				(error, context) -> {
					error.printStackTrace();
					context.registerOnError(error);
				},
				BlackboxProbe::registerOnComplete
		);
	}

	@Override
	public Long createElement(int element) {
		return random.nextLong();
	}

	@org.junit.Test
	public void someTest() {

		PublisherFactory.forEach(sub ->
						sub.onNext(random.nextLong())
		).subscribe(SubscriberFactory.unbounded(
				(data, sub) -> {
					System.out.println(data);
					sub.cancel();
				}
		));
	}
}