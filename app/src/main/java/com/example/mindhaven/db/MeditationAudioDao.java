package com.example.mindhaven.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mindhaven.models.MeditationAudio;

import java.util.List;

@Dao
public interface MeditationAudioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MeditationAudio meditationAudio);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<MeditationAudio> meditationAudios);

    @Query("SELECT * FROM meditation_audio ORDER BY title ASC")
    LiveData<List<MeditationAudio>> getAllAudios();

    @Query("SELECT * FROM meditation_audio WHERE category = :category ORDER BY title ASC")
    LiveData<List<MeditationAudio>> getAudiosByCategory(String category);

    @Query("SELECT * FROM meditation_audio WHERE isFavorite = 1 ORDER BY title ASC")
    LiveData<List<MeditationAudio>> getFavoriteAudios();

    @Query("UPDATE meditation_audio SET isFavorite = :isFavorite WHERE id = :id")
    void updateFavoriteStatus(int id, boolean isFavorite);

    @Query("DELETE FROM meditation_audio")
    void deleteAll();

    // Added this method to fix the 'Cannot resolve method getCount()' issue
    @Query("SELECT COUNT(*) FROM meditation_audio")
    int getCount();

    @Update
    void update(MeditationAudio meditationAudio);
}