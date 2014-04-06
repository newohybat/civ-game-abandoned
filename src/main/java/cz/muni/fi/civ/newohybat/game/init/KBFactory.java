package cz.muni.fi.civ.newohybat.game.init;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderConfiguration;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.conf.EventProcessingOption;
import org.drools.io.ResourceFactory;

@ApplicationScoped
public class KBFactory {
	@Produces @ApplicationScoped
	public KnowledgeBase getKnowledgeBase(){
		KnowledgeBuilderConfiguration config = KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration();
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(config);
		kbuilder.add(ResourceFactory.newClassPathResource("allRules.changeset"), ResourceType.CHANGE_SET);
		KnowledgeBaseConfiguration baseConfig = KnowledgeBaseFactory.newKnowledgeBaseConfiguration();
		baseConfig.setOption( EventProcessingOption.STREAM );
		baseConfig.setProperty("drools.assertBehaviour", "equality");
		KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase(baseConfig);
		kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
		return kbase;
	}
}
