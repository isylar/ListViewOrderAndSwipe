package com.example.android.listviewdragginganimation;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.johannblake.widgets.jbhorizonalswipelib.JBHorizontalSwipe;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PersonAdapter extends ArrayAdapter<Person> implements JBHorizontalSwipe.IJBHorizontalSwipeAdapter
{
  private final String TAG = "PersonAdapter";
  private final String TAG_TOP_VIEW = "TopView";

  final int INVALID_ID = -1;

  private List<Person> items;
  private Context context;
  private PersonListViewOrder listview;
  private JBHorizontalSwipe jbHorizontalSwipe;
  private View selectedView;
  private Person personToRemove;

  HashMap<String, Integer> idMap = new HashMap<>();

  public PersonAdapter(Context context, int textViewResourceId, List<Person> items, JBHorizontalSwipe jbHorizontalSwipe)
  {
    super(context, textViewResourceId, items);

    this.items = items;
    this.context = context;
    this.jbHorizontalSwipe = jbHorizontalSwipe;

    for (int i = 0; i < items.size(); ++i)
    {
      this.idMap.put(items.get(i).toString(), i);
    }
  }

  public void setListView(PersonListViewOrder listview)
  {
    this.listview = listview;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent)
  {
    try
    {
      View v = convertView;
      Person person = this.items.get(position);

      if (v == null)
      {
        LayoutInflater vi = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = vi.inflate(R.layout.person_item, null);
      }

      CustomListItem customListItem = (CustomListItem) v;
      customListItem.setJBHeaderRef(this.jbHorizontalSwipe);

      v.setTag(person);
      View vTop = v.findViewWithTag(TAG_TOP_VIEW);

      ImageView ivIcon = (ImageView) v.findViewById(R.id.ivIcon);

      ivIcon.setImageBitmap(person.bitmap);

      TextView tvName = (TextView) v.findViewById(R.id.tvName);
      tvName.setText(person.name);

      vTop.setOnTouchListener(onTouchListenerTopView);

      if (person.deleted)
      {
        vTop.setX(vTop.getWidth());
      }
      else
      {
        vTop.setX(0);
      }

      return v;
    }
    catch (Exception ex)
    {
      Log.e(TAG, "getView: " + ex.getMessage());
      return convertView;
    }
  }


  public void expand(final View v)
  {
    v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    final int targetHeight = v.getMeasuredHeight();

    v.getLayoutParams().height = 0;
    v.setVisibility(View.VISIBLE);

    Animation a = new Animation()
    {
      @Override
      protected void applyTransformation(float interpolatedTime, Transformation t)
      {
        v.getLayoutParams().height = interpolatedTime == 1 ? ViewGroup.LayoutParams.WRAP_CONTENT : (int) (targetHeight * interpolatedTime);
        v.requestLayout();
      }

      @Override
      public boolean willChangeBounds()
      {
        return true;
      }
    };

    // 1dp/ms
    a.setDuration((int) (targetHeight / v.getContext().getResources().getDisplayMetrics().density));
    v.startAnimation(a);
  }

  public void collapse(final View v)
  {
    final int initialHeight = v.getMeasuredHeight();

    Animation a = new Animation()
    {
      @Override
      protected void applyTransformation(float interpolatedTime, Transformation t)
      {
        if (interpolatedTime == 1)
        {
          v.setVisibility(View.GONE);
          items.remove(personToRemove);
          notifyDataSetChanged();
        }
        else
        {
          v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
          v.requestLayout();
        }
      }

      @Override
      public boolean willChangeBounds()
      {
        return true;
      }
    };

    a.setAnimationListener(animListenerCollapsedRow);

    // 1dp/ms
    a.setDuration((int) (initialHeight / v.getContext().getResources().getDisplayMetrics().density));
    v.startAnimation(a);
  }


  private Animation.AnimationListener animListenerCollapsedRow = new Animation.AnimationListener()
  {
    @Override
    public void onAnimationEnd(Animation animation)
    {
      try
      {
//        items.remove(personToRemove);
//        notifyDataSetChanged();
      }
      catch (Exception ex)
      {
        Log.e(TAG, "onAnimationEnd: " + ex.getMessage());
      }
    }

    @Override
    public void onAnimationRepeat(Animation animation)
    {

    }

    @Override
    public void onAnimationStart(Animation animation)
    {

    }
  };



  private View.OnTouchListener onTouchListenerTopView = new View.OnTouchListener()
  {
    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
      try
      {
        int action = event.getAction() & MotionEvent.ACTION_MASK;

        if (action == MotionEvent.ACTION_DOWN)
          selectedView = v;

        return false;
      }
      catch (Exception ex)
      {
        Log.e(TAG, "onCreate: " + ex.getMessage());
      }
      return false;
    }
  };


  @Override
  public View getSelectedView()
  {
    return this.selectedView;
  }

  @Override
  public int getCount()
  {
    return super.getCount();
  }

  @Override
  public long getItemId(int position)
  {
    if (position < 0 || position >= this.items.size())
    {
      return INVALID_ID;
    }

    Person person = this.items.get(position);

    long id = this.idMap.get(person.toString());

    return id;
  }

  @Override
  public boolean hasStableIds()
  {
    return true;
  }
}