package com.example.inharmony;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.common.base.Joiner;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ViewHolder> {

    private final List<Track> mTracks = new ArrayList<>();
    private final List<Artist> mArtists = new ArrayList<>();
    private final List<AlbumSimple> mAlbums = new ArrayList<>();

    private final Context mContext;
    private final ItemSelectedListener mListener;
    private String mSearchType;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView title;
        public final TextView subtitle;
        public final ImageView image;

        public ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.entity_title);
            subtitle = (TextView) itemView.findViewById(R.id.entity_subtitle);
            image = (ImageView) itemView.findViewById(R.id.entity_image);
            itemView.setOnClickListener(this);
        }

        // HOW TO DO THIS HERE
        @Override
        public void onClick(View v) {
            notifyItemChanged(getLayoutPosition());
            if (mSearchType.equals("TRACK")) {
                mListener.onItemSelectedTrack(v, mTracks.get(getAdapterPosition()));
            }
            else if (mSearchType.equals("ARTIST")) {
                mListener.onItemSelectedArtist(v, mArtists.get(getAdapterPosition()));
            }
            else if (mSearchType.equals("ALBUM")) {
                mListener.onItemSelectedAlbum(v, mAlbums.get(getAdapterPosition()));
            }

        }
    }

    public interface ItemSelectedListener {
        void onItemSelectedTrack(View itemView, Track item);
        void onItemSelectedArtist(View itemView, Artist artist);
        void onItemSelectedAlbum(View itemView, AlbumSimple album);
    }

    public SearchResultsAdapter(String searchType, Context context, ItemSelectedListener listener) {
        mContext = context;
        mListener = listener;
        mSearchType = searchType;
    }

    public void clearData() {
        mTracks.clear();
    }

    public void addDataTracks(List<Track> tracks) {
        mTracks.addAll(tracks);
        notifyDataSetChanged();
    }

    public void addDataArtists(List<Artist> artists) {
        mArtists.addAll(artists);
        notifyDataSetChanged();
    }

    public void addDataAlbums(List<AlbumSimple> albums) {
        mAlbums.addAll(albums);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (mSearchType.equals("TRACK")) {
            Log.i("BINDVIEW" , "TRACK");
            Track track = mTracks.get(position);

            holder.title.setText(track.name);

            List<String> names = new ArrayList<>();
            for (ArtistSimple i : track.artists) {
                names.add(i.name);
            }
            Joiner joiner = Joiner.on(", ");
            holder.subtitle.setText(joiner.join(names));

            Image image = track.album.images.get(0);
            if (image != null) {
                Picasso.with(mContext).load(image.url).into(holder.image);
            }
        } else if (mSearchType.equals("ARTIST")) {
            Log.i("BINDVIEW" , "ARTIST");
            Artist artist = mArtists.get(position);

            holder.title.setText(artist.name);
            Image image = artist.images.get(0);
            if (image != null) {
                Picasso.with(mContext).load(image.url).into(holder.image);
            }


        } else if (mSearchType.equals("ALBUM")) {
            Log.i("BINDVIEW" , "ALBUM");
            AlbumSimple album = mAlbums.get(position);
            AlbumSimple albums = mAlbums.get(position);

            holder.title.setText(album.name);
            List<String> names = new ArrayList<>();
            //can't do this with albumsimple :(
//            for (ArtistSimple i : album.artists) {
//                names.add(i.name);
//            }
//            Joiner joiner = Joiner.on(", ");
//            holder.subtitle.setText(joiner.join(names));
            Image image = album.images.get(0);
            if (image != null) {
                Picasso.with(mContext).load(image.url).into(holder.image);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (mSearchType.equals("TRACK")) {
            return mTracks.size();
        }
        else if (mSearchType.equals("ARTIST")) {
            return mArtists.size();
        }
        else {
            return mAlbums.size();
        }
    }
}
