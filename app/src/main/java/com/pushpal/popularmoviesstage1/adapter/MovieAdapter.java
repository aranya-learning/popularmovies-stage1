package com.pushpal.popularmoviesstage1.adapter;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pushpal.popularmoviesstage1.R;
import com.pushpal.popularmoviesstage1.activity.MainActivity;
import com.pushpal.popularmoviesstage1.database.MovieDatabase;
import com.pushpal.popularmoviesstage1.model.Movie;
import com.pushpal.popularmoviesstage1.utilities.Constants;
import com.sackcentury.shinebuttonlib.ShineButton;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = MovieAdapter.class.getSimpleName();
    private final MovieClickListener movieClickListener;
    private List<Movie> movies;
    private String arrangementType;
    private Context context;

    public MovieAdapter(List<Movie> movies, String arrangementType, MovieClickListener movieClickListener) {
        this.movies = movies;
        this.arrangementType = arrangementType;
        this.movieClickListener = movieClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        this.context = parent.getContext();

        if (arrangementType.equals(Constants.ARRANGEMENT_COMPACT))
            return new MovieCompactViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_movie_compact, parent, false));
        else if (arrangementType.equals(Constants.ARRANGEMENT_COZY))
            return new MovieCozyViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_movie_cozy, parent, false));

        return null;
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        final Movie movie = movies.get(position);
        if (arrangementType.equals(Constants.ARRANGEMENT_COMPACT)) {
            String imageURL = Constants.IMAGE_BASE_URL
                    + Constants.IMAGE_SIZE_185
                    + movie.getPosterPath();
            Picasso.with(((MovieCompactViewHolder) holder).itemView.getContext())
                    .load(imageURL)
                    //.placeholder(R.drawable.ic_launcher_foreground)
                    //.error(R.drawable.ic_launcher_background)
                    .into(((MovieCompactViewHolder) holder).moviePosterImageView);

            ((MovieCompactViewHolder) holder).voteCount
                    .setText(String.valueOf(movie.getVoteAverage()));

            ViewCompat.setTransitionName(((MovieCompactViewHolder) holder)
                    .moviePosterImageView, movie.getTitle());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    movieClickListener.onMovieClick(holder.getAdapterPosition(), movie,
                            ((MovieCompactViewHolder) holder).moviePosterImageView);
                }
            });
        } else if (arrangementType.equals(Constants.ARRANGEMENT_COZY)) {
            ((MovieCozyViewHolder) holder).movieTitle.setText(movie.getTitle());
            String date[] = movie.getReleaseDate().split("-");
            String releaseYear = date[0];
            ((MovieCozyViewHolder) holder).movieReleaseYear.setText(String.valueOf(releaseYear));

            String imageURL = Constants.IMAGE_BASE_URL
                    + Constants.IMAGE_SIZE_342
                    + movie.getPosterPath();
            Picasso.with(((MovieCozyViewHolder) holder).itemView.getContext())
                    .load(imageURL)
                    .into(((MovieCozyViewHolder) holder).moviePosterImageView);

            ViewCompat.setTransitionName(((MovieCozyViewHolder) holder)
                    .moviePosterImageView, movie.getTitle());

            ((MovieCozyViewHolder) holder).voteCount
                    .setText(String.valueOf(movie.getVoteAverage()));

            ((MovieCozyViewHolder) holder).movieLanguage
                    .setText(getLanguage(movie.getOriginalLanguage()));

            if (isFavourite(movie)) {
                ((MovieCozyViewHolder) holder).likeButton.setChecked(true);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    movieClickListener.onMovieClick(holder.getAdapterPosition(), movie,
                            ((MovieCozyViewHolder) holder).moviePosterImageView);
                }
            });

            ((MovieCozyViewHolder) holder).likeButton.setOnCheckStateChangeListener(new ShineButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(View view, boolean checked) {
                    if (checked)
                        new AddMovieToFavouritesAsync().execute(movie);
                    else
                        new RemoveMovieFromFavouritesAsync().execute(movie);
                }
            });
        }
    }

    private String getLanguage(String languageAbbr) {
        return MainActivity.languageMap.get(languageAbbr);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private boolean isFavourite(Movie movie) {
        boolean isFav = false;
        if (MainActivity.favMovies != null) {
            for (Movie favMovie : MainActivity.favMovies) {
                if (movie.getId().equals(favMovie.getId())) {
                    isFav = true;
                    break;
                }
            }
        }

        return isFav;
    }

    // Compact View Holder
    static class MovieCompactViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_movie_poster)
        ImageView moviePosterImageView;

        @BindView(R.id.tv_vote_count)
        TextView voteCount;

        MovieCompactViewHolder(View itemView) {
            super(itemView);

            // ButterKnife Binding
            ButterKnife.bind(this, itemView);
        }
    }

    // Cozy View Holder
    static class MovieCozyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_movie_poster)
        ImageView moviePosterImageView;
        @BindView(R.id.tv_movie_title)
        TextView movieTitle;
        @BindView(R.id.tv_movie_release_year)
        TextView movieReleaseYear;
        @BindView(R.id.btn_like)
        ShineButton likeButton;
        @BindView(R.id.tv_vote_count)
        TextView voteCount;
        @BindView(R.id.tv_movie_language)
        TextView movieLanguage;

        MovieCozyViewHolder(View itemView) {
            super(itemView);

            // ButterKnife Binding
            ButterKnife.bind(this, itemView);
        }
    }

    private class AddMovieToFavouritesAsync extends AsyncTask<Movie, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Movie... movies) {

            try {
                MovieDatabase.getInstance(context)
                        .getMovieDao()
                        .insertMovie(movies[0]);
            } catch (SQLiteConstraintException e) {
                Log.e(TAG, e.getMessage());

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Toast.makeText(context, "Added to favourites", Toast.LENGTH_SHORT).show();
        }
    }

    private class RemoveMovieFromFavouritesAsync extends AsyncTask<Movie, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Movie... movies) {

            MovieDatabase.getInstance(context)
                    .getMovieDao()
                    .deleteMovie(movies[0]);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Toast.makeText(context, "Removed from favourites", Toast.LENGTH_SHORT).show();
        }
    }
}