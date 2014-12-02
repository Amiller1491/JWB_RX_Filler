package com.cmps415.jwbrxfiller;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import java.lang.reflect.Field;
import java.net.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;

public class PatientReportActivity extends ActionBarActivity {

    public int currentPatient;
    public int totalPatients;
    public Document doc;
    public LinearLayout linearLayout;
    public NodeList patientList;
    public String theResponseValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_report);
        forceShowActionBarOverflowMenu();

        //create new linear layout
        linearLayout = (LinearLayout) findViewById(R.id.myLinearLayout);

        //allows connection to the internet
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //fetch the XML from site
        doc = fetchXML();

        //get list of patients and set public variables for tracking next patient
        patientList = doc.getElementsByTagName("patient_info");
        totalPatients = patientList.getLength();
        currentPatient = 0;

        //parse the XML and display patient
        displayCurrentPatientAndOrders();

        //create onClick listener for Next Patient button
        final Button nextPatientBtn = (Button) findViewById(R.id.btnNextPatient);
        nextPatientBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(currentPatient < totalPatients-1){
                    currentPatient++;
                    linearLayout.removeAllViews();
                    displayCurrentPatientAndOrders();
                } else {
                    currentPatient = 0;
                    linearLayout.removeAllViews();
                    displayCurrentPatientAndOrders();
                }
            }
        });

    }

    private void forceShowActionBarOverflowMenu() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.patient_report_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_logout:

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();

                return true;
            case R.id.action_close:

                System.exit(0);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    protected Document fetchXML() {


        try {
            URL url = new URL("http://www2.southeastern.edu/Academics/Faculty/jburris/emr.xml");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(url.openStream()));
            doc.getDocumentElement().normalize();
            return doc;
        } catch (Exception e) {
            System.out.println("Failed to fetch XML = " + e);
            return null;
        }
    }

    protected Document fillCall(String id, String medicine) {

        Intent intent = getIntent();
        final String Wnum = intent.getStringExtra("Wnum");

        try {
            URL url = new URL("http://www2.southeastern.edu/Academics/Faculty/jburris/rx_fill.php?login=" + Wnum + "&id=" + id + "&rx=" + medicine);

            //for testing a failure
            //URL url = new URL("http://www2.southeastern.edu/Academics/Faculty/jburris/rx_fill.php?");

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(url.openStream()));
            doc.getDocumentElement().normalize();
            return doc;
        } catch (Exception e) {
            System.out.println("Failed to fetch XML = " + e);
            return null;
        }

    }

    protected void displayCurrentPatientAndOrders() {

        try {
            //create the textviews that will display the patient info
            final TextView name = (TextView) findViewById(R.id.PatientName);
            final TextView pId = (TextView) findViewById(R.id.PatientID);

            //gets the current patient and all tags within it; the patient that's pulled is based on counter
            Node patientNode = patientList.item(currentPatient);

            //get attribute of patient which is the ID field
            Element patientElement = (Element) patientList.item(currentPatient);
            pId.setText(patientElement.getAttribute("id"));

            CharSequence txtId = pId.getText();
            final String id =  txtId.toString();

            //get the entire patient_info node
            Element firstPatient = (Element) patientNode;

            //get the NAME of first patient
            NodeList nameList = firstPatient.getElementsByTagName("name");
            Element nameElement = (Element) nameList.item(0);
            NodeList nameText = nameElement.getChildNodes();
            name.setText((nameText.item(0)).getNodeValue());

            //set layout params
            LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

            //get the list of orders for a patient
            NodeList orderList = patientElement.getElementsByTagName("patient_order");

            //create the textviews and buttons for the orders
            TextView[] medicineViews = new TextView[orderList.getLength()];
            TextView[] dosageViews = new TextView[orderList.getLength()];
            TextView[] remainingViews = new TextView[orderList.getLength()];
            Button[] fillButtons = new Button[orderList.getLength()];

            //loop thru the orders for a single patient
            for (int i = 0; i < orderList.getLength(); i++) {
                Node orderNode = orderList.item(i);
                Element firstOrder = (Element) orderNode;

                //medicine
                NodeList medicineList = firstOrder.getElementsByTagName("medicine");
                Element medicineElement = (Element) medicineList.item(0);
                NodeList medicineText = medicineElement.getChildNodes();

                CharSequence txtMedicine = (medicineText.item(0)).getNodeValue();
                final String medicine =  txtMedicine.toString();

                medicineViews[i] = new TextView(this);
                medicineViews[i].setText("RX:     " + (medicineText.item(0)).getNodeValue());
                medicineViews[i].setTextSize(23);
                medicineViews[i].setLayoutParams(lp);
                linearLayout.addView(medicineViews[i]);

                //dosage
                NodeList dosageList = firstOrder.getElementsByTagName("dosage");
                Element dosageElement = (Element) dosageList.item(0);
                NodeList dosageText = dosageElement.getChildNodes();
                dosageViews[i] = new TextView(this);
                dosageViews[i].setText("Dosage:     " + (dosageText.item(0)).getNodeValue());
                dosageViews[i].setTextSize(23);
                dosageViews[i].setLayoutParams(lp);
                linearLayout.addView(dosageViews[i]);

                //remaining
                NodeList remainingList = firstOrder.getElementsByTagName("refillsRemaining");
                Element remainingElement = (Element) remainingList.item(0);
                NodeList remainingText = remainingElement.getChildNodes();
                remainingViews[i] = new TextView(this);
                remainingViews[i].setText("Remaining:     " + (remainingText.item(0)).getNodeValue());
                remainingViews[i].setTextSize(23);
                remainingViews[i].setLayoutParams(lp);
                linearLayout.addView(remainingViews[i]);

                fillButtons[i] = new Button(this);
                fillButtons[i].setText("Fill");
                fillButtons[i].setTextSize(23);
                fillButtons[i].setLayoutParams(lp);
                fillButtons[i].setMinWidth(620);
                linearLayout.addView(fillButtons[i]);

                // Fill button disabled if no fills available
                String medString = (remainingText.item(0)).getNodeValue();
                if (medString.equals("0")){
                    fillButtons[i].setEnabled(false);
                }

                fillButtons[i].setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        try {
                            //make callout to web service passing in the patientId and medicine name
                            Document calloutResponse = fillCall(id, medicine);

                            //re-fetchXML
                            doc = fetchXML();
                            patientList = doc.getElementsByTagName("patient_info");

                            NodeList responseList = calloutResponse.getElementsByTagName("return");
                            Node responseNode = responseList.item(0);
                            //do more shit to get the reponse text
                            Element responseElement = (Element) responseNode;
                            NodeList reponseText = responseElement.getChildNodes();
                            theResponseValue = reponseText.item(0).getNodeValue(); //this is a public variable

                            if(theResponseValue.equals("failure")){
                                Context context = getApplicationContext();
                                CharSequence text = "ERROR FILLING RX";
                                int duration = Toast.LENGTH_LONG;

                                Toast toast = Toast.makeText(context, text, duration);
                                TextView vToast = (TextView) toast.getView().findViewById(android.R.id.message);
                                vToast.setTextColor(Color.RED);
                                vToast.setTextSize(23);
                                toast.show();
                            }

                            //clear out the view
                            linearLayout.removeAllViews();
                            //display updated patient info
                            displayCurrentPatientAndOrders();
                        } catch (Exception e) {
                            System.out.println("Failed to fetch XML = " + e);
                        }
                    }
                });
            }
        } catch (Exception e) {
            System.out.println("Failed to Parse XML = " + e);
        }
    }
}