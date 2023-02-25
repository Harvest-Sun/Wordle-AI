//
//public class Evaluation {
//
//	private int mask;
//	
//	public Evaluation() {
//		mask = 0;
//	}
//	
//	public boolean isEmpty(int pos) {
//		return (mask & 3 << pos * 2) == 0;
//	}
//	
//	public boolean isAbsent(int pos) {
//		return (mask & 3 << pos * 2) >> pos * 2 == 1;
//	}
//	
//	public boolean isPresent(int pos) {
//		return (mask & 3 << pos * 2) >> pos * 2 == 2;
//	}
//	
//	public boolean isCorrect(int pos) {
//		return (mask & 3 << pos * 2) >> pos * 2 == 3;
//	}
//	
//	public void setEmpty(int pos) {
//		mask &= -1 ^ 3 << pos * 2;
//	}
//	
//	public void setAbsent(int pos) {
//		mask |= 1 << pos * 2;
//	}
//	
//	public void setPresent(int pos) {
//		mask |= 2 << pos * 2;
//	}
//	
//	public void setCorrect(int pos) {
//		mask |= 3 << pos * 2;
//	}
//	
//	public boolean equals(Object o) {
//		return mask == ((Evaluation) o).mask;
//	}
//	
//	public int hashCode() {
//		return mask;
//	}
//
//}
