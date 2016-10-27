package zomifi.op27no2.printlogo;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Andrew on 12/29/2015.
 * Used for Customer Assign application for Zoomifi Inc.
 */

public class GridViewAdapter extends ArrayAdapter<String>
{
    private Context context;
    private List<String> ipeNames;
    private SharedPreferences prefs;
    private SharedPreferences.Editor edt;

    public GridViewAdapter(Context context, List<String> ipeNames)
    {
        super(context, R.layout.custom_gridview_block, ipeNames);

        this.context = context;
        this.ipeNames = ipeNames;


    }

    @Override
    public String getItem(int position)
    {
        return ipeNames.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        String ipeName = ipeNames.get(position);
        TextView textView;

        if(convertView == null)
        {
            LayoutInflater inflater = LayoutInflater.from(this.context);

            convertView = inflater.inflate(R.layout.custom_gridview_block, null);
            convertView.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT, 150));

            textView = (TextView)convertView.findViewById(R.id.ipeName);

            prefs = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE);
            edt = prefs.edit();

            if(prefs.getBoolean("active"+position, false) == true) {
                textView.setBackgroundResource(R.drawable.red_button);
            }
            else{
                textView.setBackgroundResource(R.drawable.aqua_green_button);
            }

            convertView.setTag(new Container(textView));
        }
        else
        {
            Container container = (Container)convertView.getTag();

            textView = container.getDriverName();

            prefs = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE);
            edt = prefs.edit();

            if(prefs.getBoolean("active"+position, false) == true) {
                textView.setBackgroundResource(R.drawable.red_button);
            }
            else{
                textView.setBackgroundResource(R.drawable.aqua_green_button);
            }
        }

        if(ipeName.equals("Click to Add")) {
            textView.setText(ipeName);
            textView.setSingleLine(false);
        } else{
            textView.setText(ipeName);
            textView.setSingleLine();
            textView.setSelected(true);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setHorizontallyScrolling(true);
        }

        return convertView;
    }

    private class Container
    {
        private TextView ipeName;

        public Container(TextView ipeName)
        {
            this.ipeName = ipeName;
        }

        public TextView getDriverName()
        {
            return this.ipeName;
        }
    }
}
