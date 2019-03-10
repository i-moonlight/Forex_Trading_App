package be.kdg.mobile_client.room.overview;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import be.kdg.mobile_client.BaseActivity;
import be.kdg.mobile_client.MenuActivity;
import be.kdg.mobile_client.R;
import be.kdg.mobile_client.room.Room;
import be.kdg.mobile_client.room.RoomService;
import be.kdg.mobile_client.shared.SharedPrefService;
import be.kdg.mobile_client.user.UserService;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class PrivateRoomOverviewActivity extends BaseActivity {
    @BindView(R.id.tvOverviewHeader) TextView tvOverviewHeader;
    @BindView(R.id.btnBack) Button btnBack;
    @BindView(R.id.btnEdit) Button btnEdit;
    @BindView(R.id.lvUser) RecyclerView lvRoom;
    @BindView(R.id.progressBar) ProgressBar progressBar;
    @Inject RoomService roomService;
    @Inject UserService userService;
    @Inject SharedPrefService sharedPrefService;
    private RoomRecyclerAdapter roomAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getControllerComponent().inject(this);
        setContentView(R.layout.activity_overview_private);
        ButterKnife.bind(this);
        addEventHandlers();
        getRooms();
    }

    private void addEventHandlers() {
        btnBack.setOnClickListener(e -> navigateTo(MenuActivity.class));
        //TODO: add edit button.
    }

    /**
     * Gets all the private rooms that a specific user owns or that the user is authorized to view
     * and join.
     * <p>
     * If the rooms are loaded, that the progress bar will disappear
     */
    public void getRooms() {
        Observable<List<Room>> roomObs = roomService.getPrivateRooms().observeOn(AndroidSchedulers.mainThread());
        tvOverviewHeader.setText(getString(R.string.privateRooms).toUpperCase());

        compositeDisposable.add(roomObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::initializeAdapter, error -> {
                    Toast.makeText(this, getString(R.string.failed_to_connect), Toast.LENGTH_LONG).show();
                    Log.e("PrivateRoomOverviewActivity", error.getMessage());
                }));
    }

    /**
     * Initializes the rooms adapter to show all the rooms that
     * were retrieved from the game-service back-end.
     *
     * @param rooms The rooms that need to be used by the adapter.
     */
    private void initializeAdapter(List<Room> rooms) {
        compositeDisposable.add(userService.getUser("")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(myself -> {
                    progressBar.setVisibility(View.GONE);
                    roomAdapter = new RoomRecyclerAdapter(this, rooms, myself);
                    lvRoom.setAdapter(roomAdapter);
                    lvRoom.setLayoutManager(new LinearLayoutManager(this));
                }));
    }

    /**
     * If the activity starts again than we need to check if the user is still authorized.
     */
    @Override
    protected void onResume() {
        checkIfAuthorized(sharedPrefService);
        if (roomAdapter != null) roomAdapter.notifyDataSetChanged(); // refresh rooms
        super.onResume();
    }
}
