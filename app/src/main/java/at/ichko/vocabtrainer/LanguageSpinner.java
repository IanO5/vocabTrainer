package at.ichko.vocabtrainer;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class LanguageSpinner implements AdapterView.OnItemSelectedListener {

    private Context context;
    private Spinner spinner;
    private Table table;
    ItemSelectExecution itemSelected;
    ItemNothingSelectExecution nothingSelected;

    private final String prefTableId = "tableid";

    public LanguageSpinner(Spinner spinner, Context context, ItemSelectExecution e, ItemNothingSelectExecution nothingSelected){
        this.context = context;
        this.spinner = spinner;
        this.itemSelected = e;
        this.nothingSelected = nothingSelected;
        table = new Table(context);
        this.spinner.setOnItemSelectedListener(this);
    }

    public void refresh(){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, table.getTableNames());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setSelection(table.getTableIndex());
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        SharedPreferences prefTable = context.getSharedPreferences(prefTableId, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefTable.edit();

        editor.putInt(prefTableId, i);
        editor.commit();

        spinner.setSelection(i);

        itemSelected.execute();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        nothingSelected.nothingSelectedExecute();
    }
}
