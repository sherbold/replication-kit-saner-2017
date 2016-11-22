package weka_predictor;

import weka.classifiers.Classifier;

public class WekaClassifiers {
	
	public String classifier_name_;
	public String weka_call_;
	public Classifier weka_classifier_;
	public String weka_class_;
	
	public WekaClassifiers(String classifier_name, String classifier_options, Classifier weka_classifier, String weka_class)
	{
		weka_class_ = weka_class;
		classifier_name_ = classifier_name;
		weka_call_ = classifier_options;
		weka_classifier_ = weka_classifier;
	}

	public WekaClassifiers(String classifier_name, String classifier_options, Classifier weka_classifier)
	{
		classifier_name_ = classifier_name;
		weka_call_ = classifier_options;
		weka_classifier_ = weka_classifier;
	}
}
