package com.omniscient.log4jcontrib.swingappender.ui.test;

import java.util.ArrayList;
import java.util.List;

public class SingletonInstantiator {

	private List threads;

	public SingletonInstantiator() {
		threads = new ArrayList();
		for (int count = 1; count < 5; count++) {
			threads.add(new Thread() {
				public void run() {
					for (int i = 0; i < 100; i++) {
						ClassicSingleton instance = ClassicSingleton.getInstance();
					}
				}
			});
		}
	}

	public void runTest() {
		for (int i = 0; i < threads.size(); i++) {
			((Thread) threads.get(i)).start();
		}
	}

	public void waitThreadsToDie() {
		for (int i = 0; i < threads.size(); i++) {
			try {
				((Thread) threads.get(i)).join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private static class ClassicSingleton {
		private static ClassicSingleton instance = new ClassicSingleton();

		private ClassicSingleton() {
			// Exists only to defeat instantiation.
		}

		public static synchronized ClassicSingleton getInstance() {
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return instance;
		}
	}

	public static void main(String[] args) {
		SingletonInstantiator instantiator = new SingletonInstantiator();
		long start = System.currentTimeMillis();
		instantiator.runTest();
		instantiator.waitThreadsToDie();
		long diff = System.currentTimeMillis() - start;
		System.out.println("Test took: " + diff + " ms." );
	}
}