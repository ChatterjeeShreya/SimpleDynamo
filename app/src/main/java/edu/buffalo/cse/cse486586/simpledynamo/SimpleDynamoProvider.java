package edu.buffalo.cse.cse486586.simpledynamo;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Formatter;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

import static android.content.ContentValues.TAG;

public class SimpleDynamoProvider extends ContentProvider {

	static final String[] REMOTE_PORT = {"11124", "11112", "11108", "11116", "11120"};
	static final String[] HASHES = {"177ccecaec32c54b82d5aaafc18a2dadb753e3b1", "208f7f72b198dadd244e61801abe1ec3a4857bc9", "33d6357cfaaf0f72991b0ecd8c56da066613c089", "abf0fd8db03e5ecb199a9b82929e9db79b909643", "c25ddd596aa7c81fa12378fa725f706d54325d12"};

	static final int SERVER_PORT = 10000;
	TreeMap hashPort = new TreeMap();
	TreeMap<String, ArrayList<String>> lookup = new TreeMap<String, ArrayList<String>>();
	ArrayList<String> ports = new ArrayList<String>(Arrays.asList("11124", "11112", "11108", "11116", "11120"));
    ArrayList<String> hashes = new ArrayList<String>(Arrays.asList("177ccecaec32c54b82d5aaafc18a2dadb753e3b1", "208f7f72b198dadd244e61801abe1ec3a4857bc9", "33d6357cfaaf0f72991b0ecd8c56da066613c089", "abf0fd8db03e5ecb199a9b82929e9db79b909643", "c25ddd596aa7c81fa12378fa725f706d54325d12"));


	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		String selectedPort = "";
		Integer counter = 0;

		if(selection.equals("@")) {
			File[] files = getContext().getFilesDir().listFiles();
			if (files != null) {
				for (File file : files) {
					file.delete();
				}
			}
		}

		else if(selection.equals("*")){
			Collection<String> keys = hashPort.values();


			try{
				for(String port : keys){
					Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(port));

					String msgToSend = "delete";
					DataOutputStream writeObj =
							new DataOutputStream(socket.getOutputStream());
					writeObj.writeUTF(msgToSend);


				}
			}catch(Exception e){
                e.printStackTrace();
			}

		}

		else{
			try {

				String deletehash = genHash(selection);
				Object[] keyset = hashPort.keySet().toArray();
				Object[] valueset = hashPort.values().toArray();

				while(true){
					if(counter == keyset.length){
						counter = 0;
						break;
					}
					if(deletehash.compareTo(String.valueOf(keyset[counter])) < 0){
						break;
					}
					else{
						counter += 1;
					}
				}
				int ctr = 0;

				while(ctr<=2) {

					selectedPort = String.valueOf(valueset[counter%keyset.length]);
                    try {
                        Socket socket = null;

                        socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                Integer.parseInt(selectedPort));

                        String msgToSend = "del" + "-" + deletehash;
                        DataOutputStream writeObj = null;

                        writeObj = new DataOutputStream(socket.getOutputStream());

                        writeObj.writeUTF(msgToSend);
                    }catch (IOException e){
                        e.printStackTrace();
                    }
					counter+=1;
					ctr++;
				}

			}catch(NoSuchAlgorithmException e){
				e.printStackTrace();
			}
		}



		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub

		String key = (String) values.get("key");
		String value = (String) values.get("value");
		String hashkey = "inithashkey";
		int comp;
		String selectedPort = "initport", selectedHash = "inithash";
		String msg;
		int map_size = hashPort.size(), counter = 0;
		boolean flag = false;

		try {
			hashkey = genHash(key);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}



		Object[] keyset = hashPort.keySet().toArray();
		Object[] valueset = hashPort.values().toArray();

		while(true){
			if(counter == keyset.length){
				counter = 0;
				break;
			}
			if(hashkey.compareTo(String.valueOf(keyset[counter])) < 0){
				break;
			}
			else{
				counter += 1;
			}
		}

		int ctr = 0;
		while (ctr<=2)
		{
			selectedPort = String.valueOf(valueset[counter%keyset.length]);
			//Log.e("hell", valueset[counter].toString());
			Socket socket = null;
			try {
				socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
						Integer.parseInt(selectedPort));
                String msgToSend = "insert" + "-" + hashkey + "-" + key + "-" + value;

                DataOutputStream writeObj = null;
                writeObj = new DataOutputStream(socket.getOutputStream());
                DataInputStream readObj = new DataInputStream(socket.getInputStream());

                Log.e("own", msgToSend + " " + Integer.parseInt(selectedPort)/2);
                writeObj.writeUTF(msgToSend);

            } catch (IOException e) {
				Log.e("hello", "error");
			}



			/*if(counter == keyset.length){
				counter = 0;

			}
			else
			{
				counter+=1;
			}*/
			counter+=1;
			ctr++;

		}
		return null;
	}



	@Override
	public boolean onCreate() {
		for (int i = 0; i < REMOTE_PORT.length; i++) {
			hashPort.put(HASHES[i], REMOTE_PORT[i]);
		}
		Log.e("qqq", hashPort.toString());
		String dynamo = null;
		try {
			dynamo = genHash("OldmUknJzK0KKO3Se4zzYwO6DxL3I3Bx");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		// TODO Auto-generated method stub
		TelephonyManager tel = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
		String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
		final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));

		try {

			ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
			new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);

		} catch (IOException e) {
			Log.e(TAG, "Can't create a ServerSocket");
			return false;
		}

		new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, myPort);

		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
						String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub

		MatrixCursor mcursor = new MatrixCursor(new String[]{"key", "value"});
		// TODO Auto-generated method stub
		String hashkey="";
		String keyFile, valFile;
		String value;

		String hash= "inithashkey";
		int comp;
		String selectedPort = "initport", selectedHash = "inithash";
		String msg;
		int map_size = hashPort.size(), counter = 0;
		boolean flag = false;


		// * case
		if (selection.equals("*")) {

				Set<String> keys = hashPort.keySet();
				Integer count = 0;
				for (String k : keys) {
					value = (String) hashPort.get(k);
                    try {
                        Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                Integer.parseInt(value));
                        String msgToSend = "Query" + "-" + "all";
                        DataOutputStream writeObj =
                                new DataOutputStream(socket.getOutputStream());
                        DataInputStream readOb = null;
                        writeObj.writeUTF(msgToSend);

                        readOb = new DataInputStream(socket.getInputStream());
                        String response = readOb.readUTF();
                        Log.e("reponse", response);

                        if (response.equals("norows")) {
                            Log.e("yey", "no rows");
                        } else {
                            Log.e("yey", response);
                            String[] queryres = response.split("-");
                            //Log.e("after", String.valueOf(queryres.length));


                            String[] allkeys = new String[]{};
                            String[] allvalues = new String[]{};
                            ArrayList tempkeys = new ArrayList();
                            ArrayList tempvalues = new ArrayList();

                            if (queryres[0].contains(",")) {

                                allkeys = queryres[0].split(",");
                                allvalues = queryres[1].split(",");

                                for (int j = 0; j < allkeys.length; j++) {
                                    allkeys[j] = allkeys[j].trim();
                                    allvalues[j] = allvalues[j].trim();
                                }

                                tempkeys = new ArrayList(Arrays.asList(allkeys));
                                tempvalues = new ArrayList(Arrays.asList(allvalues));

                                Log.e("after", String.valueOf(allkeys.length));
                                Log.e("after", String.valueOf(allvalues.length));
                            } else {

                                tempkeys.add(queryres[0].trim());
                                tempvalues.add(queryres[1].trim());

                            }


                            for (int i = 0; i < tempkeys.size(); i++) {
                                mcursor.addRow(new Object[]{tempkeys.get(i), tempvalues.get(i)});
                                Log.e("results", tempkeys.get(i) + " " + tempvalues.get(i));
                            }

                        }
                    }catch (IOException e){
                        e.printStackTrace();
                    }
				while (mcursor.moveToNext()) {
					count += 1;

				}

			}
            return mcursor;

		}



		// @ case
		else if (selection.equals("@")) {

			Set<String> keys = lookup.keySet();

			for (String k : keys) {

                ///////////////////////////////////////////////////////
				keyFile = k;
				valFile = (String) lookup.get(k).get(0);
				String readFile = k;
				FileInputStream fin = null;

				try {
					fin = getContext().openFileInput(readFile);
				} catch (FileNotFoundException e3) {
					e3.printStackTrace();
				}
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(fin));
				String readValue = null;
				try {
					readValue = bufferedReader.readLine();
				} catch (IOException e3) {
					e3.printStackTrace();
				}
                mcursor.addRow(new Object[]{valFile, readValue});
				////////////////////////////////////////////////////////

				// keep either above or just below linec
				//mcursor.addRow(new Object[]{lookup.get(k).get(0), lookup.get(k).get(1)});


			}

		}


		//Particular key lookup case
		else
        {
            try {
                hashkey = genHash(selection);
                Object[] keyset = hashPort.keySet().toArray();
                Object[] valueset = hashPort.values().toArray();

                while (true) {
                    if (counter == keyset.length) {
                        counter = 0;
                        break;
                    }
                    if (hashkey.compareTo(String.valueOf(keyset[counter])) < 0) {
                        break;
                    } else {
                        counter += 1;
                    }
                }

                selectedPort = String.valueOf(valueset[counter]);
                try {
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(selectedPort));
                    String msgToSend = "query" + "-" + hashkey + "-" + selection;
                    DataOutputStream writeObj = new DataOutputStream(socket.getOutputStream());
                    DataInputStream readOb = new DataInputStream(socket.getInputStream());

                    writeObj.writeUTF(msgToSend);
                    String queryres = readOb.readUTF();
                    String[] keyvalue = queryres.split("-");
                    mcursor.addRow(new Object[]{keyvalue[0], keyvalue[1]});

                } catch (IOException e) {
                    Log.e("finallyfuckoff", hashkey + "-" + selection + "-" + Integer.parseInt(ports.get((counter + 1) % ports.size())) / 2);
                    try {
                        Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                Integer.parseInt(ports.get((counter + 1) % ports.size())));
                        String msgToSend = "query" + "-" + hashkey + "-" + selection;
                        DataOutputStream writeObj = new DataOutputStream(socket.getOutputStream());
                        DataInputStream readOb = new DataInputStream(socket.getInputStream());
                        writeObj.writeUTF(msgToSend);
                        String queryres = readOb.readUTF();
                        String[] keyvalue = queryres.split("-");
                        mcursor.addRow(new Object[]{keyvalue[0], keyvalue[1]});
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }catch (NoSuchAlgorithmException e){
                e.printStackTrace();
            }




		}
		return mcursor;
	}


	@Override
	public int update(Uri uri, ContentValues values, String selection,
					  String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}


	private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

		@Override
        /* References:-
            https://docs.oracle.com/javase/tutorial/networking/sockets/clientServer.html
           */
		protected Void doInBackground(ServerSocket... sockets) {
			ServerSocket serverSocket = sockets[0];

			Socket clientSocket = null;
			while (true) {

				try {
					clientSocket = serverSocket.accept();
					//clients.add(clientSocket);
                    DataOutputStream writeObj = new DataOutputStream(clientSocket.getOutputStream());



					DataInputStream readObj = null;

					readObj = new DataInputStream(clientSocket.getInputStream());


					String msgReceived = readObj.readUTF();


					String arr[] = msgReceived.split("-");

					/*
					if(arr[0].equals("prev1")){
                        Set<String> keys = lookup.keySet();
                        for (String k : keys) {

                            String hashedKey = k;
                            if(arr[1].equals("11112")){
                                if(hashedKey.compareTo(arr[3])>0){
                                    String original = lookup.get(k).get(0);
                                    String value = lookup.get(k).get(1);

                                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                            Integer.parseInt(arr[1]));
                                    String msgToSend = "insert" + "-" + "aaaa" + hashedKey + "-" + original + "-" + value;
                                    writeObj =
                                            new DataOutputStream(socket.getOutputStream(), true);

                                    writeObj.writeUTF(msgToSend);
                                }
                            }else if(hashedKey.compareTo(arr[2]) < 0 && hashedKey.compareTo(arr[3])>0){
                                String original = lookup.get(k).get(0);
                                String value = lookup.get(k).get(1);

                                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                        Integer.parseInt(arr[1]));
                                String msgToSend = "insert" + "-" +  "aaaa" + hashedKey + "-" + original + "-" + value;
                                writeObj =
                                        new DataOutputStream(socket.getOutputStream(), true);

                                writeObj.writeUTF(msgToSend);
                            }

                        }
                    }
                    if(arr[0].equals("prev2")){
                        Set<String> keys = lookup.keySet();
                        for (String k : keys) {

                            String hashedKey = k;
                            if(arr[1].equals("11108")){
                                if(hashedKey.compareTo(arr[3])>0){
                                    String original = lookup.get(k).get(0);
                                    String value = lookup.get(k).get(1);

                                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                            Integer.parseInt(arr[1]));
                                    String msgToSend = "insert" + "-" + "aaaa" + hashedKey + "-" + original + "-" + value;
                                    writeObj =
                                            new DataOutputStream(socket.getOutputStream(), true);

                                    writeObj.writeUTF(msgToSend);
                                }
                            }else if(hashedKey.compareTo(arr[2]) <= 0 && hashedKey.compareTo(arr[3])>=0){
                                String original = lookup.get(k).get(0);
                                String value = lookup.get(k).get(1);

                                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                        Integer.parseInt(arr[1]));
                                String msgToSend = "insert" + "-" + "aaaa" + hashedKey + "-" + original + "-" + value;
                                writeObj =
                                        new DataOutputStream(socket.getOutputStream(), true);

                                writeObj.writeUTF(msgToSend);
                            }

                        }
                    }*/
                    if (arr[0].equals("insert")) {
                        String hashkey = arr[1];
                        String value = arr[3];
                        String original = arr[2];

                        ArrayList<String> temp = new ArrayList<String>();
                        temp.add(original);
                        temp.add(value);

                        if(lookup.containsKey(hashkey)){
                            lookup.remove(hashkey);
                            lookup.put(hashkey, temp);
                        }else{
                            lookup.put(hashkey, temp);
                        }

                        FileOutputStream outputStream;

                        Log.e("insert", msgReceived);

                        try {
                            outputStream = getContext().openFileOutput(hashkey, Context.MODE_PRIVATE);
                            outputStream.write(value.getBytes());
                            Log.e("ServInsfilenm", hashkey );
                            Log.e("ServInsstring",value );
                            outputStream.close();
                            //Log.e("shreya","file write");
                        } catch (Exception e) {
                            Log.e(TAG, "File write failed");
                        }
                    }

                    if (arr[0].equals("next")) {
                        Log.e("www", msgReceived);

                        Set<String> keys = lookup.keySet();
                        for(String k : keys){
                            String hashedKey = k;
                            Integer counter = 0;
                            while(true){
                                if(counter == ports.size()){
                                    counter = 0;
                                    break;
                                }
                                if(k.compareTo(String.valueOf(hashes.get(counter))) < 0){
                                    break;
                                }
                                else{
                                    counter += 1;
                                }
                            }

                            if(ports.get(counter).equals(arr[1])){
                                String original = lookup.get(k).get(0);
                                String value = lookup.get(k).get(1);
                                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                        Integer.parseInt(arr[1]));
                                String msgToSend = "insert" + "-" + hashedKey + "-" + original + "-" + value;
                                writeObj =
                                        new DataOutputStream(socket.getOutputStream());

                                writeObj.writeUTF(msgToSend);
                            }

                        }

                    }


                    if (arr[0].equals("prev1")) {
                        Log.e("www", msgReceived);

                        Set<String> keys = lookup.keySet();
                        for(String k : keys){
                            String hashedKey = k;
                            Integer counter = 0;
                            while(true){
                                if(counter == ports.size()){
                                    counter = 0;
                                    break;
                                }
                                if(k.compareTo(String.valueOf(hashes.get(counter))) < 0){
                                    break;
                                }
                                else{
                                    counter += 1;
                                }
                            }

                            if(ports.get(counter).equals(arr[2])){
                                String original = lookup.get(k).get(0);
                                String value = lookup.get(k).get(1);
                                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                        Integer.parseInt(arr[1]));
                                String msgToSend = "insert" + "-" + hashedKey + "-" + original + "-" + value;
                                writeObj =
                                        new DataOutputStream(socket.getOutputStream());

                                writeObj.writeUTF(msgToSend);
                            }

                        }

                    }

                    /*
                    if (arr[0].equals("next")) {
                        Log.e("next", arr[1] + "-" + arr[2] + "-" + arr[3] + "-" + arr[4]);

                        Set<String> keys = lookup.keySet();
                        for (String k : keys) {

                            String hashedKey = k;
                            if(arr[2].equals("11124")){
                                if(hashedKey.compareTo(arr[3])>0){
                                    String original = lookup.get(k).get(0);
                                    String value = lookup.get(k).get(1);

                                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                            Integer.parseInt(arr[2]));
                                    String msgToSend = "insert" + "-" + "aaaa" + hashedKey + "-" + original + "-" + value;
                                    writeObj =
                                            new DataOutputStream(socket.getOutputStream(), true);

                                    writeObj.writeUTF(msgToSend);
                                }
                            }else if(hashedKey.compareTo(arr[1]) <= 0 && hashedKey.compareTo(arr[3]) >= 0){
                                String original = lookup.get(k).get(0);
                                String value = lookup.get(k).get(1);

                                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                        Integer.parseInt(arr[2]));
                                String msgToSend = "insert" + "-" + "aaaa" + hashedKey + "-" + original + "-" + value;
                                writeObj =
                                        new DataOutputStream(socket.getOutputStream(), true);

                                writeObj.writeUTF(msgToSend);
                            }


                        }
                    }*/


					if(msgReceived.equals("delete")){
						Log.e("delete", "delete req");
                        lookup = new TreeMap<String, ArrayList<String>>();
						File[] files = getContext().getFilesDir().listFiles();
						if(files != null) {
							for (File file : files) {
								file.delete();
							}
						}
					}

					if(arr[0].equals("del")){
						Log.e("del", "Delete key");
						lookup = new TreeMap<String, ArrayList<String>>();
						String deletehash = arr[1];

						File dir = getContext().getFilesDir();
						File file = new File(dir, deletehash);
						file.delete();
					}



					if (arr[0].equals("query")) {
						Log.e("sent", "ndndndnnd");
						String readFile = arr[1];
						FileInputStream fin = null;

						fin = getContext().openFileInput(readFile);
						BufferedReader bufferedReader = new BufferedReader(
								new InputStreamReader(fin));
						String readValue = bufferedReader.readLine();
						Log.e("sent", "ejjje");
						DataOutputStream writeOb = new DataOutputStream(clientSocket.getOutputStream());
						writeOb.writeUTF(arr[2] + "-" + readValue);
						Log.e("sent", "msg sent");
					}

					if (arr[0].equals("Query")) {
						File[] files = getContext().getFilesDir().listFiles();
						ArrayList allkeys = new ArrayList();
						ArrayList allValues = new ArrayList();
						String allkeystr = "";
						String allvalstr = "";
						Set<String> keys = lookup.keySet();
						if (files != null) {
							for (File file : files) {
								FileInputStream fin = null;
								fin = getContext().openFileInput(file.getName());
								BufferedReader bufferedReader = new BufferedReader(
										new InputStreamReader(fin));
								String readValue = bufferedReader.readLine();
								String valFile = "";
								for (String k : keys) {

									if (k.equals(file.getName())) {
										valFile = (String) lookup.get(k).get(0);
										allkeys.add(valFile);
										allValues.add(readValue);
									}

								}


								///Log.e("queryallllll", valFile + " " + readValue);

							}
						}
						DataOutputStream writeOb = new DataOutputStream(clientSocket.getOutputStream());

						if (allkeys.size() == 0) {
							writeOb.writeUTF("norows");
						} else {
							String keystring = allkeys.toString().substring(1, allkeys.toString().length() - 1);
							String valstring = allValues.toString().substring(1, allValues.toString().length() - 1);

							Log.e("bbb", allkeys.toString());
							Log.e("bbb", keystring);
							Log.e("bbb", "***8");
							Log.e("bbb", allValues.toString());
							Log.e("bbb", valstring);

							writeOb.writeUTF(keystring + "-" + valstring);
						}
					}


				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {

            Integer index = ports.indexOf(msgs[0]);
            Log.e("index", index.toString());

            lookup = new TreeMap<String, ArrayList<String>>();
            try{
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(msgs[0]));
                String msgToSend = "delete";
                DataOutputStream writeObj =
                        new DataOutputStream(socket.getOutputStream());

                writeObj.writeUTF(msgToSend);

            }catch (IOException e){
                e.printStackTrace();
            }

            try{
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(ports.get((index + 1) % ports.size())));
                String msgToSend = "next" + "-" + ports.get(index) + "-" +  ports.get((index + 1) % ports.size());
                DataOutputStream writeObj =
                        new DataOutputStream(socket.getOutputStream());

                writeObj.writeUTF(msgToSend);

            }catch (IOException e){
                e.printStackTrace();
            }

            try{
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(ports.get((index + 4) % ports.size())));
                String msgToSend = "prev1" + "-" + ports.get(index) + "-" + ports.get((index + 4) % ports.size());
                DataOutputStream writeObj =
                        new DataOutputStream(socket.getOutputStream());

                writeObj.writeUTF(msgToSend);

            }catch (IOException e){
                e.printStackTrace();
            }

            try{
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(ports.get((index + 3) % ports.size())));
                String msgToSend = "prev1" + "-" + ports.get(index) + "-" + ports.get((index + 3) % ports.size());
                DataOutputStream writeObj =
                        new DataOutputStream(socket.getOutputStream());

                writeObj.writeUTF(msgToSend);

            }catch (IOException e){
                e.printStackTrace();
            }
            /*
            try{
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(ports.get((index + 1) % ports.size())));
                String msgToSend = "next" + "-" + hashes.get(index) + "-" + ports.get(index) + "-" + hashes.get((index + 4) % ports.size()) + "-" + ports.get((index + 4) % ports.size()) ;
                DataOutputStream writeObj =
                        new DataOutputStream(socket.getOutputStream(), true);

                writeObj.writeUTF(msgToSend);

            }catch (IOException e){
                e.printStackTrace();
            }

            try{
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(ports.get((index + 4) % ports.size())));
                String msgToSend = "prev1" + "-" + ports.get(index) + "-" + hashes.get((index + 4) % ports.size()) + "-" + hashes.get((index + 3) % ports.size()) ;
                DataOutputStream writeObj =
                        new DataOutputStream(socket.getOutputStream(), true);

                writeObj.writeUTF(msgToSend);

            }catch (IOException e){
                e.printStackTrace();
            }

            try{
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(ports.get((index + 3) % ports.size())));
                String msgToSend = "prev2" + "-" + ports.get(index) + "-" + hashes.get((index + 3) % ports.size()) + "-" + hashes.get((index + 2) % ports.size()) ;
                DataOutputStream writeObj =
                        new DataOutputStream(socket.getOutputStream(), true);

                writeObj.writeUTF(msgToSend);

            }catch (IOException e){
                e.printStackTrace();
            }*/
            /*
            try {

                for (int i = 0; i < REMOTE_PORT.length; i++) {
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(REMOTE_PORT[i]));
                    String msgToSend = "Joining" + "-" + msgs[0];
                    DataOutputStream writeObj =
                            new DataOutputStream(socket.getOutputStream(), true);

                    Log.e("Client msgtosend", msgToSend);
                    writeObj.writeUTF(msgToSend);

                    //Re-reading
                    BufferedReader readOb = null;
                    try {
                        readOb = new BufferedReader(
                                new InputStreamReader(socket.getInputStream()));
                        String pNum = readOb.readLine();
                        Log.d("clientread", pNum);
                    } catch (IOException ie) {
                        ie.printStackTrace();
                    }
                }
            }
            // arr[i] = pNum + "-" + remotePort[i];
            catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
            }*/
            return null;
        }
    }


	private String genHash(String input) throws NoSuchAlgorithmException {
		MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
		byte[] sha1Hash = sha1.digest(input.getBytes());
		Formatter formatter = new Formatter();
		for (byte b : sha1Hash) {
			formatter.format("%02x", b);
		}
		return formatter.toString();
	}
}

