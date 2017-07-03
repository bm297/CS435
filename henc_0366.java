import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.*;


public class henc_0366{

	// 256 ASCII Characters
	private static final int NUM_CHAR = 256;

	// Huffman Tree
	private static class Node implements Comparable<Node> {
		private char character;
		private int frequency;
		private Node left, right;

		Node(char character, int frequency, Node left, Node right) {
			this.character = character;
			this.frequency = frequency;
			this.left = left;
			this.right = right;
		}

	private boolean isLeaf() {	// Check if node is leaf node
		return (left == null) && (right == null);
	}

	public int compareTo(Node that) { // Compare the frequency
		return this.frequency - that.frequency;
	}
	}	// Node Class

	public static class BufferedByteWriter { // Write single byte to output file

	    private byte currentByte;             // The byte that is being filled
	    private byte numBitsWritten;          // Number of bits written to the current byte
	    private BufferedOutputStream output;  // The output byte stream

	    public BufferedByteWriter(String pathName) throws FileNotFoundException { // Constructor, takes in filepath as an arg
	    	currentByte = 0;
	    	numBitsWritten = 0;
	    	output = new BufferedOutputStream(new FileOutputStream(pathName));
	    }
	    
	    public void writeBit(int bit) throws IOException { // Forming a byte from individual bits
	    	if (bit < 0 || bit > 1)
	    		throw new IllegalArgumentException("Argument to writeBit: bit = " + bit);

	    	numBitsWritten++;
	        currentByte |= bit << (8 - numBitsWritten); // Shift bit to the left to its appropriate location
	        if (numBitsWritten == 8) { // Have we got a full byte?
	            output.write(currentByte); // Write the full byte to the output file
	            numBitsWritten = 0; // Reset bit counter
	            currentByte = 0; // Reset byte buffer
	        }
	    }

	    public void close() throws IOException { // BufferedWriter MUST BE CLOSED
	        output.write(currentByte); // Writes out the current byte buffer
	        output.write(numBitsWritten); // Writes out bit counter FOR DECOMPRESSION
	        output.close();
	    }
	}

	public static class BufferedBitReader {

	    int current;   // Current byte being returned, bit by bit
	    int next;      // Next byte to be returned (could be a count)
	    int afterNext; // Byte two after the current byte
	    int bitMask;   // Shows which bit to return

	    BufferedInputStream input;

	    public BufferedBitReader(String pathName) throws IOException {
	    	input = new BufferedInputStream(new FileInputStream(pathName));

	    	current = input.read();
	    	if (current == -1)
	    		throw new EOFException("File did not have two bytes");

	    	next = input.read();
	    	if (next == -1)
	    		throw new EOFException("File did not have two bytes");

	    	afterNext = input.read();
	        bitMask = 128; // a 1 in leftmost bit position
	    }

	    public int readBit() throws IOException {
	        int returnBit; // Hold the bit to return

	        if (afterNext == -1) // Are we emptying the last byte?

	            /*
	             *  When afterNext == -1, next is the count of bits remaining.
	             */

	            if (next == 0) // No more bits in the final byte to return
	                return -1; // No more bits to return
	                else {
	                	if ((bitMask & current) == 0)
	                		returnBit = 0;
	                	else
	                		returnBit = 1;

	                next--;                 // One fewer bit to return
	                bitMask = bitMask >> 1; // Shift to mask next bit
	                return returnBit;
	            }
	            else {
	            	if ((bitMask & current) == 0)
	            		returnBit = 0;
	            	else
	            		returnBit = 1;

	            bitMask = bitMask >> 1;  // Shift to mask next bit

	            if (bitMask == 0) {      // Finished returning this byte?
	                bitMask = 128;       // Leftmost bit next
	                current = next;
	                next = afterNext;
	                afterNext = input.read();
	            }
	            return returnBit;
	        }
	    }

	    public void close() throws IOException {
	    	input.close();
	    }
	}

	public static Node compress(String fn) throws Exception{ // Compress file

		byte[] bytes = Files.readAllBytes(Paths.get(fn)); // Array of all chars in file

		int[] freq = new int[NUM_CHAR]; // Freq array
		for(int i=0; i<bytes.length; i++){ // Count frequency of each char
			if (bytes[i] >= 0){
			//System.out.println(bytes[i]);
			freq[bytes[i]]++;
			} else{
				//System.out.println(bytes[i]);
				int unsigned = bytes[i] & 255;
				//System.out.println(unsigned);
				//System.out.println(unsigned);
				freq[unsigned]++;
			}
			//System.out.println(freq[i]); GOOD
		}
		//System.out.println(bytes[195]);

		Node root = buildTree(freq); // Build Huffman Tree
		String[] st = new String[NUM_CHAR];	// Array of 256 strings
		huffmanTable(st, root, ""); // Convert Huffman tree into table where code (e.g. right is 0 and left is 1 of each node)

		int bit = 0; // Bit flag(0|1)
		BufferedByteWriter bitOutput = new BufferedByteWriter(fn + ".huf"); // Instanstiate byte writer class with filepath as an arg
		for(int i=0; i<bytes.length; i++){ // Iterate over all read char
			
			String code;
			if(bytes[i] >= 0)

			code = st[bytes[i]]; // Access the char compression code
			else{
				int unsigned = bytes[i] & 255;
				System.out.println(bytes[i]);
				code = st[bytes[unsigned]];
			}

			for(int j=0; j<code.length(); j++){ // Iterate over char compression code
				bit = code.charAt(j); // Bit is the current char compression code(48|49) because charAt() returns ASCII representation of 0 and 1
				if(bit == 48) bit = 0;  // Update bit(0|1)
				else bit = 1;
				bitOutput.writeBit(bit);
				//bit = 0;
			}
		}
		bitOutput.close(); // MUST CLOSE WRITEBUFFER FOR DECOMPRESSION
		return root;
	}

	public static void decompress(String fn) throws Exception{

		String originalFile = fn.replace(".huf", "");
		
		Node root = compress(originalFile);

		String content;

		content = new String(Files.readAllBytes(Paths.get(fn)));
		//String originalFile = fn.replace(".huf", "");

		FileWriter bf = new FileWriter(originalFile);
		BufferedWriter bw = new BufferedWriter(bf);
		//System.out.println(bytes[11]);
		Node temp = root;
		BufferedBitReader bitInput = new BufferedBitReader(fn);

		int readBit = bitInput.readBit();
		while(readBit != -1){

			if(readBit == 0) temp = temp.left;
			else if(readBit == 1) temp = temp.right;
			
			if(temp.isLeaf()){
				//System.out.println(temp.character);
				bw.write(temp.character);
				temp = root;
			}
			readBit = bitInput.readBit();
		}
		bw.close();
	}

	private static Node buildTree(int[] freq){ // Build Huffman Tree based on frequencies

		MinPQ<Node> pq = new MinPQ<Node>(); // instantiate MinHeap priority Queue
		for(char i=0; i<NUM_CHAR; i++){
			if(freq[i] > 0) pq.insert(new Node(i, freq[i], null, null));
		} 

		while(pq.size() > 1){ // Combine right and left node into single node
			Node left = pq.remove();
			Node right = pq.remove();
			Node parent = new Node('\0', left.frequency + right.frequency, left, right);
			pq.insert(parent);
		}
		return pq.remove(); // Return node of Huffman Tree
	}

	private static void huffmanTable(String[] st, Node x, String s){
		if(!x.isLeaf()){
			huffmanTable(st, x.left, s + '0');
			huffmanTable(st, x.right, s + '1');
		}
		else{
			st[x.character] = s;
		}
	}

	public static void main(String[] args) throws Exception {

		if (args[0].equals("henc")) {
			//need to save file extenstion and add .huf
			compress(args[1]);
		}
		else if (args[0].equals("hdec"))
			decompress(args[1]);
        	//need to delete .huf file after completion
		else
			throw new IllegalArgumentException("Illegal command line argument");

} // main
} // henc_0366

