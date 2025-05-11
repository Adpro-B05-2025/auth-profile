package id.ac.ui.cs.advprog.authprofile.service;

import id.ac.ui.cs.advprog.authprofile.dto.request.UpdateProfileRequest;
import id.ac.ui.cs.advprog.authprofile.dto.response.ProfileResponse;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public interface IProfileService {

    /**
     * Get the current logged-in user's profile
     * @return the profile response
     */
    ProfileResponse getCurrentUserProfile();

    /**
     * Get a specific user's profile by ID
     * @param userId the user ID
     * @return the profile response
     */
    ProfileResponse getUserProfile(Long userId);

    /**
     * Get all CareGivers
     * @return list of all care givers' profiles
     */
    List<ProfileResponse> getAllCareGivers();

    /**
     * Search CareGivers by name and speciality
     * @param name the name to search for (optional)
     * @param speciality the speciality to search for (optional)
     * @return list of matching care givers' profiles
     */
    List<ProfileResponse> searchCareGivers(String name, String speciality);

    /**
     * Update the current user's profile
     * @param updateRequest the update request details
     * @return the updated profile response
     */
    ProfileResponse updateCurrentUserProfile(UpdateProfileRequest updateRequest);

    /**
     * Delete the current user's account
     */
    void deleteCurrentUserAccount();


    /**
     * Get all CareGivers with only essential information for listing
     * @return list of all care givers with minimal information
     */
    List<ProfileResponse> getAllCareGiversLite();

    /**
     * Search CareGivers by name and speciality with only essential information
     * @param name the name to search for (optional)
     * @param speciality the speciality to search for (optional)
     * @return list of matching care givers with minimal information
     */
    List<ProfileResponse> searchCareGiversLite(String name, String speciality);

    /**
     * Get a specific caregiver's profile by ID with sensitive information hidden
     * @param caregiverId the caregiver ID
     * @return the lite profile response with sensitive fields hidden
     */
    ProfileResponse getCareGiverProfileLite(Long caregiverId);

    /**
     * Search CareGivers by name, speciality, and/or working schedule
     * @param name the name to search for (optional)
     * @param speciality the speciality to search for (optional)
     * @param dayOfWeek the day of week to check availability (optional)
     * @param time the time to check availability (optional)
     * @return list of matching care givers with minimal information
     */
    List<ProfileResponse> searchCareGiversLite(String name, String speciality, DayOfWeek dayOfWeek, LocalTime time);
}