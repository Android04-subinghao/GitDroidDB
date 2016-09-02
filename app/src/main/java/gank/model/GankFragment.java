package gank.model;


import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.fuicui.gitdroid.gitdroid.R;
import com.fuicui.gitdroid.gitdroid.commons.ActivityUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class GankFragment extends Fragment implements GankPresenter.GankView{
    @BindView(R.id.tvDate)
    TextView tvDate;
    @BindView(R.id.btnFilter)
    ImageButton btnFilter;
    @BindView(R.id.content)
    ListView content;
    @BindView(R.id.emptyView)
    FrameLayout emptyView;

    private Date date;
    private Calendar mCalendar;
    private SimpleDateFormat mSimpleDateFormat;
    private GankAdapter mAdapter;
    private ActivityUtils mActivityUtils;
    private GankPresenter mPresenter;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityUtils=new ActivityUtils(this);
        mCalendar=Calendar.getInstance(Locale.CHINA);
        //获取当前时间
        date=new Date(System.currentTimeMillis());
        mPresenter=new GankPresenter(this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       View view=inflater.inflate(R.layout.fragment_gank,container,false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);
        //规范日期格式
        mSimpleDateFormat=new SimpleDateFormat("yyyy-MM-dd",Locale.CHINA);
        tvDate.setText(mSimpleDateFormat.format(date));

        mAdapter = new GankAdapter();
        content.setAdapter(mAdapter);
        content.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String url = mAdapter.getItem(position).getUrl();
                mActivityUtils.startBrowser(url);
            }
        });


        mPresenter.getGanks(date);

    }

    @OnClick(R.id.btnFilter)
    public void showDateDialog(View view){
        int year=mCalendar.get(Calendar.YEAR);
        int month=mCalendar.get(Calendar.MONTH);
        int day=mCalendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog=new DatePickerDialog(getContext(),dateSetListener,year,month,day);
        datePickerDialog.show();
    }
    private DatePickerDialog.OnDateSetListener dateSetListener=new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mCalendar.set(year,monthOfYear,dayOfMonth);
            date=mCalendar.getTime();
            tvDate.setText(mSimpleDateFormat.format(date));
            mPresenter.getGanks(date);
        }
    };

    @Override
    public void setData(List<GankItem> list) {
        mAdapter.setDatas(list);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void showEmptyView() {
        emptyView.setVisibility(View.VISIBLE);
        content.setVisibility(View.GONE);
    }

    @Override
    public void showMessage(String msg) {
        mActivityUtils.showToast(msg);
    }

    @Override
    public void hideEmptyView() {
        emptyView.setVisibility(View.GONE);
        content.setVisibility(View.VISIBLE);
    }
}
