package banner.postprocessing;

import banner.types.Sentence;

import java.util.ArrayList;
import java.util.List;

public class SequentialPostProcessor implements PostProcessor
{
	List<PostProcessor> processors;

	public SequentialPostProcessor()
	{
		processors = new ArrayList<PostProcessor>();
	}

	public void addPostProcessor(PostProcessor processor)
	{
		processors.add(processor);
	}

	public void postProcess(Sentence sentence)
	{
		for (PostProcessor processor : processors)
			processor.postProcess(sentence);
	}

}
