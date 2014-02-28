package com.whereismydot;


import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;



public class App 
{
    public static void main( String[] args )
    {
    	String text = "Game of Thrones was highly anticipated by fans before its premiere.[87][88] It has since become a critical and commercial success. The critical response to the three aired seasons of Game of Thrones has been very positive. All seasons were listed on several yearly \"best of\" lists published by US media, such as the Washington Post (2011), TIME (2011 and 2012) and The Hollywood Reporter (2012).[100][101][102] Seasons 2 and 3 obtained a Metacritic rating of more than 80, which the website rates \"universal acclaim\". In 2013, the Writers Guild of America placed Game of Thrones in the fortieth place on the list of the 101 best-written TV series.[103] The performance of the very large, predominantly British and Irish cast was widely praised. American Peter Dinklage's \"charming, morally ambiguous, and self-aware\"[104] portrayal of Tyrion, which won him an Emmy and a Golden Globe award, among others, was particularly noted. \"In many ways, \"Game of Thrones\" belongs to Dinklage\", wrote the L.A. Times[105] even before, in season 2, the \"scene-stealing actor's\"[106] character became the series' most central figure.[106] Several critics highlighted the performances of the women[105] and child actors.[107] 14-year-old Maisie Williams, already noted in the first season for her debut performance as Arya Stark, received particular praise for her work opposite veteran actor Charles Dance (Tywin Lannister) in season 2.[108]";

    	
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        

        Annotation annotation = pipeline.process(text);
        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            Tree tree = sentence.get(SentimentCoreAnnotations.AnnotatedTree.class);
            int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
            
            System.out.println(sentence.toString() + "=>" + sentiment);

        }
        
    }
}
