package org.camunda.bpm.getstarted.chargecard;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.client.ExternalTaskClient;

public class ChargeCardWorker {
	private final static Logger LOGGER = Logger.getLogger(ChargeCardWorker.class.getName());

	public static void main(String[] args) {
		ExternalTaskClient client = ExternalTaskClient.create().baseUrl("http://localhost:8080/engine-rest")
				.asyncResponseTimeout(10000) // long polling timeout
				.build();

		// subscribe to an external task topic as specified in the process
		client.subscribe("charge-card").lockDuration(1000) // the default lock duration is 20 seconds, but you can
															// override this
				.handler((externalTask, externalTaskService) -> {
					// Put your business logic here
					try {
						// Get a process variable
						String item = (String) externalTask.getVariable("item");
						Long amount = (Long) externalTask.getVariable("amount");

						if (amount < 0) {

							LOGGER.info("Amount is < 0, throwing BPMN Error with code \"kaputt\"");
							externalTaskService.handleBpmnError(externalTask, "kaputt");
						} else {
							LOGGER.info("Charging credit card with an amount of '" + amount + "'€ for the item '" + item
									+ "'...");

							// Thread.sleep(12000);

							// Complete the task
							externalTaskService.complete(externalTask);
						}
					} catch (Exception e) {
						LOGGER.log(Level.SEVERE, "Could not process Payment Retrieval request");
						externalTaskService.handleFailure(externalTask, "Could not process Payment Retireval request",
								e.getMessage(), 0, 500);
					}
				}).open();
	}
}