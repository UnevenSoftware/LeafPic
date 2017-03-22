package org.horaapps.leafpic.new_way;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.horaapps.leafpic.util.StringUtils;

import java.util.Arrays;
import java.util.Locale;

public class Query {

    public Uri uri;
    public String[] projection;
    public String selection;
    public String[] args;
    public String sort;
    public boolean ascending = true;
    public int limit = -1;

    Query(Builder builder) {
        uri = builder.uri;
        projection = builder.projection;
        selection = builder.selection;
        args = builder.args;
        sort = builder.sort;
        ascending = builder.ascending;
    }

    public Cursor getCursor(ContentResolver cr) {
        Log.wtf("asd",hack());
        return cr.query(uri, projection, selection, args, hack());
    }

    private String hack() {
        if (sort == null && limit == -1) return null;
        return StringUtils.join(" ", sort, sortOrder(), limit());
    }

    private String limit() {
        return limit == -1 ? "" : String.format(Locale.CANADA,"LIMIT %d", limit);
    }

    private String sortOrder() {
        return ascending ? "ASC" : "DESC";
    }

    public static final class Builder {
        Uri uri;
        String[] projection;
        String selection;
        String[] args;
        String sort;
        int limit = -1;
        public boolean ascending = true;


        public Builder() {
        }

        public Builder uri(Uri val) {
            uri = val;
            return this;
        }

        public Builder projection(String[] val) {
            projection = val;
            return this;
        }

        public Builder selection(String val) {
            selection = val;
            return this;
        }

        public Builder args(String[] val) {
            args = val;
            return this;
        }

        public Builder sort(String val) {
            sort = val;
            return this;
        }

        public Builder limit(int val) {
            limit = val;
            return this;
        }

        public Builder ascending(boolean val) {
            ascending = val;
            return this;
        }

        public Query build() {
            return new Query(this);
        }

        public Cursor cursor(ContentResolver cr) {
            return build().getCursor(cr);
        }
    }

    @Override
    public String toString() {
        return "Query{" +
                "\nuri=" + uri +
                "\nPROJECTION=" + Arrays.toString(projection) +
                "\nselection='" + selection + '\'' +
                "\nargs=" + Arrays.toString(args) +
                "\nsort='" + sort  +" "+ sortOrder()+ '\'' +
                "\nlimit='" + limit + '\'' +
                '}';
    }
}