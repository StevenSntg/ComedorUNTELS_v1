package pe.edu.untels.comedor.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import pe.edu.untels.comedor.R
import pe.edu.untels.comedor.data.model.MenuItem
import pe.edu.untels.comedor.databinding.ItemWeeklyMenuBinding

class WeeklyMenuAdapter : ListAdapter<MenuItem, WeeklyMenuAdapter.MenuViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemWeeklyMenuBinding.inflate(inflater, parent, false)
        return MenuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MenuViewHolder(private val binding: ItemWeeklyMenuBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MenuItem) {
            binding.tvDayName.text = item.dayName
            binding.tvHighlight.text = item.highlight
            binding.tvMainCourse.text = item.mainCourse
            binding.tvSoup.text = item.soup
            binding.tvDessert.text = item.dessert
            binding.tvBeverage.text = item.beverage
            binding.tvCalories.text =
                binding.root.context.getString(R.string.menu_calories, item.calories)
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<MenuItem>() {
        override fun areItemsTheSame(oldItem: MenuItem, newItem: MenuItem): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: MenuItem, newItem: MenuItem): Boolean =
            oldItem == newItem
    }
}
