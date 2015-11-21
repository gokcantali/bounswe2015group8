package com.cmpe.bounswe2015group8.westory.front;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.cmpe.bounswe2015group8.westory.R;
import com.cmpe.bounswe2015group8.westory.back.MemberLocalStore;
/**
 * Main activity overviewing all operations. Contains an action bar and navigation sidebar.
 * Is responsible for fragment management and all operations related to action bar and sidebar.
 * @author xyllan
 * Date: 01.11.2015.
 */
public class MainActivity extends AppCompatActivity {
    public static void beginFragment(FragmentActivity a, NamedFragment f) {
        beginFragment(a,f,true);
    }
    public static void beginFragment(FragmentActivity a, NamedFragment f, boolean addToBackstack) {
        FragmentTransaction ft = a.getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame, f, f.getName());
        if(addToBackstack) ft.addToBackStack(f.getName());
        ft.commit();
    }
    MemberLocalStore memberLocalStore;
    FragmentManager fm;
    NavigationView navBarView;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        memberLocalStore = new MemberLocalStore(this);
        fm = this.getSupportFragmentManager();
        navBarView = (NavigationView) findViewById(R.id.navigation);
        setOnItemClickListener(navBarView);
        resetNavbar();

        drawerLayout = (DrawerLayout) findViewById(R.id.main_drawer);
        drawerToggle = new ActionBarDrawerToggle(this,drawerLayout,R.string.generic_description,R.string.generic_description);
        drawerLayout.setDrawerListener(drawerToggle);
        Toolbar t = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(t);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        MainActivity.beginFragment(this, new HomeFragment());
        //MainActivity.beginFragment(this, new HeritagesFragment());
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void resetNavbar() {
        Menu menu = navBarView.getMenu();
        MenuItem item = menu.getItem(menu.size() - 1);
        if(authenticated()) item.setTitle(getResources().getString(R.string.nav_logout));
        else item.setTitle(getResources().getString(R.string.nav_login));
    }
    private boolean authenticated() {
        return memberLocalStore.getUserLoggedIn();
    }
    private void setOnItemClickListener(NavigationView nv) {
        final MainActivity a = this;
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                drawerLayout.closeDrawer(navBarView);
                NamedFragment nf = null;
                switch (item.getItemId()) {
                    case R.id.navHome:
                        nf = new HomeFragment();
                        break;
                    case R.id.navHeritages:
                        nf = new HeritagesFragment();
                        break;
                    case R.id.navAddHeritage:
                        nf = new HeritageEditFragment();
                        break;
                    case R.id.navPosts:
                        nf = new PostsFragment();
                        break;
                    case R.id.navLogin:
                        if (authenticated()) {
                            memberLocalStore.clearMemberData();
                            nf = new HomeFragment();
                        } else {
                            nf = new LoginFragment();
                        }
                        resetNavbar();
                        break;
                    default:
                        break;
                }
                MainActivity.beginFragment(a, nf);
                return true;
            }
        });
    }
}
