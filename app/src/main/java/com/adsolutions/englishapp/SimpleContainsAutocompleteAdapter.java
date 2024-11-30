package com.adsolutions.englishapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class SimpleContainsAutocompleteAdapter<T> extends ArrayAdapter<T> implements Filterable {
    private List<T> listObjects;
    List<T> suggestions = new ArrayList<>();
    private Context context;

    public SimpleContainsAutocompleteAdapter(Context context, List<T> listObjects) {
        super(context, R.layout.list_item_simple, listObjects);
        this.listObjects = new ArrayList<>(listObjects);
        this.context = context;
    }

    private Filter mFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (constraint != null) {
                suggestions.clear();
                for (T object : listObjects) {
                    if (object.toString().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        suggestions.add(object);
                    }
                }

                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
            }
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results == null) {
                return;
            }
            List<T> filteredList = (List<T>) results.values;
            if (results.count > 0) {
                clear();
                for (T filteredObject : filteredList) {
                    add(filteredObject);
                }
                notifyDataSetChanged();
            }
        }
    };

    @NonNull
    @Override
    public Filter getFilter() {
        return mFilter;
    }

    private static class ViewHolder {
        TextView title;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Object listObject = getItem(position);
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item_simple, parent, false);
            viewHolder.title = convertView.findViewById(R.id.text_view_simple);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (listObject != null) {
            viewHolder.title.setText(listObject.toString());
        }
        return convertView;
    }
}