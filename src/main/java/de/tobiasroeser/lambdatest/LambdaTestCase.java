package de.tobiasroeser.lambdatest;

public interface LambdaTestCase {

	String getName();

	String getSuiteName();

	Optional<Section> getSection();

}
