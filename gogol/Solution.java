package abstracts;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


/**
 * @author william le cam
 * Coding Game Layer to easily handle most of the features requested : multilines reading, reactiveness, memoization, etc.
 */
public class Solution {
	

	////////////////////////SCANNER-OPTIONS////////////////////////
	static int linesPerQuestion = 1;
	static boolean faultTolerant= false; //if algo fail -> return error string
	static Interactivity interactive = Interactivity.react;
	static AnswerType answerType = AnswerType.pure;
	static String death = "WRONG_ANSWER";
	
	static int linesPerContext = 2;
	static boolean initiative = true;
	
	
	
	////////////////////////SCANNER-MODEL////////////////////////
	static enum Interactivity{inc,batch,react}
	static enum AnswerType{case_,pure}
	
	////////////////////////SCANNER////////////////////////
	public static void main(String[] args) {
		Scanner in = new Scanner(new BufferedReader(new InputStreamReader(System.in)));
		int size = numberOfCases(in);
		List<Question> qs = new ArrayList<>();
		for(int i = 0; i< size; i++){
			if(interactive==Interactivity.react){
				Context c = formatInput(in, s -> new Context(s), linesPerContext);
				List<Entry> acc = new ArrayList<>();
				int max = initiative ? c.size+2 : c.size;
				for(int j = 0; j< max; j++){
					AbstractAnswer a =  (initiative && j==0) ?
							solveAndFaultTolerantAndMemorize(c, acc, new NoQuestion() )
							:solveAndFaultTolerantAndMemorize(c, acc, formatInput(in, s -> new Question(s), linesPerQuestion));
					if(a.isPrintable())
					System.out.println(formatOutput(i,j,a));
					System.out.flush();
					if(a instanceof AnswerStop) break;
				}
			}else{
				Question q = formatInput(in, s -> new Question(s), linesPerQuestion);
				qs.add(q);
				if(interactive==Interactivity.inc){
					AbstractAnswer a = solveAndFaultTolerantAndMemorize(qs.get(i));
					System.out.println(formatOutput(i,0,a));
				}
			}
		}
		if(interactive==Interactivity.batch)
			IntStream.range(0,size).forEach(i -> System.out.println(formatOutput(i,0,solveAndFaultTolerantAndMemorize(qs.get(i)))));
	}
	
	static int numberOfCases(Scanner in){
		int i = in.nextInt(); in.nextLine();
		return i;
	}
	static <T> T formatInput(Scanner in, Function<String[],T> cons, int limit){
		String[] output = new String[limit];
		for (int i = 0; i < limit; i++) {
			output[i]=in.nextLine();
			if(output[i].equals(death))
				System.exit(0);
		}
		return cons.apply(output);
	}
	static String formatOutput(int i,int j, AbstractAnswer answer){
		String print = "";
		String data = Stream.<String>of(answer.toStrings()).collect(Collectors.joining(" "));
		if(answerType==AnswerType.case_)
			print="Case #" + (i+1) + ": " + data;
		else
			print=data;
		return print;
	}
	
	////////////////////////SCANNER-BASED////////////////////////
	static class Entry{
		Entry(AbstractQuestion q, Answer a){this.q=q;this.a=a;}
		AbstractQuestion q;
		Answer a;
	}

	static AbstractAnswer solveAndFaultTolerantAndMemorize(AbstractQuestion q){
		return solveAndFaultTolerantAndMemorize(new Context(), new ArrayList<>(),q);
	}
	static AbstractAnswer solveAndFaultTolerantAndMemorize(Context context, List<Entry> cache, AbstractQuestion q){
		AbstractAnswer a = new AnswerError();
		try{
			a = q instanceof NoQuestion ? solveNoQuestion(context, cache, (NoQuestion)q) : solveQuestion(context,cache,(Question)q);
			if(interactive==Interactivity.react && a instanceof Answer)
				cache.add(new Entry(q,(Answer)a));
		}catch(Exception e){
			if(!faultTolerant) throw e;
		}
		return a;
	}
	

	////////////////////////FORMATS-ABSTRACTS////////////////////////
	static abstract class AbstractAnswer{
		abstract String[] toStrings();
		boolean isPrintable(){return true;}
	}
	static class AnswerError extends AbstractAnswer{
		 String[] toStrings(){return new String[] {"Error"};};
	}
	static abstract class AbstractQuestion{}
	static class NoQuestion extends AbstractQuestion{}
	
	
	
	////////////////////////ALGO-MODEL////////////////////////
	static class Context  {
		Context(){}
		Context(String[] lines) {
			String[] l0 = lines[0].split(" ");
			low = Integer.valueOf(l0[0]);up = Integer.valueOf(l0[1]);
			size = Integer.valueOf(lines[1]);
		}
		int low;
		int up;
		int size;
	}
	static class Question extends AbstractQuestion{
		Question(String[] lines) {
			this.judgement= lines[0];
		}
		String judgement;
	}
	static class Answer extends AbstractAnswer{
		public Answer(Integer i) {
			this.i = i;
		}
		int i ;

		String[] toStrings() {
			return new String[] {String.valueOf(i)};
		}
	}
	static class AnswerStop extends AbstractAnswer{
		
		public AnswerStop() {
		}
		boolean isPrintable(){return false;}


		String[] toStrings() {
			return null;
		}
		
	}

	////////////////////////ALGO-MODEL-EXTENSION////////////////////////

	////////////////////////ALGO-MAIN////////////////////////
	static AbstractAnswer solveNoQuestion(Context context, List<Entry> cache, NoQuestion q){
		return new Answer(
				(context.low+context.up+1)/2
				);
	}
	static AbstractAnswer solveQuestion(Context context, List<Entry> cache, Question q){

		
		if(q.judgement.equals("CORRECT")) {
			return new AnswerStop();
		}
		if(q.judgement.equals("TOO_SMALL")) {
			int res = (cache.get(cache.size()-1).a.i
					+context.up+1
					)/2;
			context.low= cache.get(cache.size()-1).a.i;
			return new Answer(
					res
					);
		}else {
			int res = (cache.get(cache.size()-1).a.i
					+context.low
					)/2;
			context.up=cache.get(cache.size()-1).a.i;
			return new Answer(
					res
					);
		}

		
	}

	////////////////////////ALGO-FUNCTIONS////////////////////////
	Stream<Entry> getEntry(List<Entry> cache, BiPredicate<AbstractQuestion,AbstractAnswer> p){
		return cache.stream().filter(e -> p.test(e.q, e.a));
	}

	
}
